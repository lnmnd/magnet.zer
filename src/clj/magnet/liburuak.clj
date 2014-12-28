(ns ^{:doc "Domeinua: liburuak."}
  magnet.liburuak
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [clojure.data.codec.base64 :as b64]
            [clojure.java.io :as io]
            [magnet.lagun :refer [oraingo-data orriztatu]]
            [magnet.saioak :refer [lortu-saioa]]
            [magnet.torrent :as torrent]))

(defn- fitx-sortu!
  "Edukia base64 formatuan eta fitxategiaren izena emanda fitxategia sortzen du."
  [base64 fitx]
  (with-open [out (io/output-stream fitx)]
    (->> base64
         .getBytes
         b64/decode
         (.write out))))

(defn- baliozko-liburu-eskaera
  "Liburuak beharrezko eremu guztiak dituen edo ez"
  [lib]
  (and 
   (every? #(contains? lib %)
           [:epub :titulua :egileak :hizkuntza :sinopsia :urtea :etiketak :azala])
   (not (= "" (:epub lib)))
   (not (= "" (:azala lib)))))

(defn- lortu-liburua [kon id]
  (if-let [lib (first (sql/query kon ["select id, magnet, erabiltzailea, titulua, hizkuntza, sinopsia, argitaletxea, urtea, generoa, azala, igotze_data, (select count(liburua) as iruzkin_kopurua from iruzkinak where liburua=?) as iruzkin_kopurua, (select count(liburua) as gogoko_kopurua from gogokoak where liburua=?) as gogoko_kopurua from liburuak where id=?" id id id]))]
    (assoc lib
      :egileak (map :egilea (sql/query kon ["select egilea from liburu_egileak where liburua=?" id]))
      :etiketak (map :etiketa (sql/query kon ["select etiketa from liburu_etiketak where liburua=?" id])))
    nil))

(defn- liburuak [db-kon idak]
  (pmap (fn [x] (lortu-liburua db-kon (:id x))) idak))

(defn- liburua-gehitu! [db-kon partekatu kokapenak torrent-gehitze-programa trackerrak edukia]
  (sql/with-db-transaction [kon db-kon]
    (let [edukia (assoc edukia
                   :argitaletxea (if (nil? (:argitaletxea edukia))
                                   "" (:argitaletxea edukia))
                   :generoa (if (nil? (:generoa edukia))
                              "" (:generoa edukia))
                   :data (oraingo-data)
                   :iruzkin_kopurua 0
                   :gogoko_kopurua 0)]
      (do (sql/insert! kon :liburuak
                       [:erabiltzailea :magnet :titulua :hizkuntza :sinopsia :argitaletxea :urtea :generoa :azala :igotze_data]
                       [(:erabiltzailea edukia) "aldatuko-da" (:titulua edukia) (:hizkuntza edukia)
                        (:sinopsia edukia) (:argitaletxea edukia) (:urtea edukia) (:generoa edukia)
                        "aldatuko-da" (:data edukia)])
          (let [id (:id (first (sql/query kon "select identity() as id")))
                epub-fitx (str (:epub-karpeta kokapenak) id ".epub")
                torrent-fitx (str (:torrent-karpeta kokapenak) id ".epub.torrent")                
                azal-fitx (str (:irudi-karpeta kokapenak) id ".jpg")
                azal-url (str (:irudi-url kokapenak) id ".jpg")]
            (doseq [egi (:egileak edukia)]
              (sql/insert! kon :liburu_egileak
                           [:liburua :egilea]
                           [id egi]))
            (doseq [eti (:etiketak edukia)]
              (sql/insert! kon :liburu_etiketak
                           [:liburua :etiketa]
                           [id eti]))
            (fitx-sortu! (:epub edukia) epub-fitx)
            (let [magnet (torrent/sortu! trackerrak epub-fitx torrent-fitx)]
              (sql/update! kon :liburuak
                           {:magnet magnet}
                           ["id=?" id])
              (when partekatu
                (torrent/partekatu! torrent-gehitze-programa torrent-fitx (:torrent-karpeta kokapenak)))
              (fitx-sortu! (:azala edukia) azal-fitx)
              (sql/update! kon :liburuak
                           {:azala azal-url}
                           ["id=?" id])
              {:liburua (assoc edukia :id id :magnet magnet :azala azal-url)}))))))

(defn liburua-aldatu!
  "id duen liburua aldatzen du."
  [db-kon id edukia]
  (let [argitaletxea (if (nil? (:argitaletxea edukia))
                        "" (:argitaletxea edukia))
        generoa (if (nil? (:generoa edukia))
                  "" (:generoa edukia))]
    (sql/with-db-transaction [kon db-kon]
      (sql/update! kon :liburuak
                   {:titulua (:titulua edukia)
                    :hizkuntza (:hizkuntza edukia)
                    :sinopsia (:sinopsia edukia)
                    :argitaletxea argitaletxea
                    :urtea (:urtea edukia)
                    :generoa generoa
                    :azala (:azala edukia)}
                   ["id=?" id])
      (lortu-liburua kon id))))

(defn gehitu!
  "Liburua gehitzen du."
  [saio-osa db-kon partekatu kokapenak torrent-gehitze-programa trackerrak token edukia]
  (if (baliozko-liburu-eskaera edukia)
    (if-let [{erabiltzailea :erabiltzailea} (lortu-saioa saio-osa token)]
      [:ok (liburua-gehitu! db-kon partekatu kokapenak torrent-gehitze-programa trackerrak (assoc edukia :erabiltzailea erabiltzailea))]
      [:baimenik-ez])
    [:ezin-prozesatu]))

(defn lortu
  "Eskatutako id-a duen liburua lortu"
  [db-kon id]
  (if-let [lib (lortu-liburua db-kon id)]
    [:ok {:liburua lib}]
    [:ez-dago]))

(defn aldatu!
  "id bat eta edukia emanda liburua aldatu"
  [saio-osa db-kon token id edukia]
  (if (baliozko-liburu-eskaera edukia)
    (let [[egoera lib] (lortu db-kon id)]
      (if (= egoera :ez-dago)
        [:ez-dago]
        (if-let [era (:erabiltzailea (lortu-saioa saio-osa token))]
          (if (= era (:erabiltzailea (:liburua lib)))
            [:ok {:liburua (liburua-aldatu! db-kon id (assoc edukia :erabiltzailea era))}] 
            [:baimenik-ez])
          [:baimenik-ez])))
    [:ezin-prozesatu]))

(defn ezabatu!
  "id bat emanda liburua ezabatu"
  [saio-osa db-kon token id]
  (sql/with-db-transaction [kon db-kon]
    (let [[egoera lib] (lortu db-kon id)]
      (if (= egoera :ez-dago)
        [:ez-dago]
        (if-let [era (:erabiltzailea (lortu-saioa saio-osa token))]
          (if (= era (:erabiltzailea (:liburua lib)))
            (do (sql/delete! kon :liburuak ["id=?" id])
                [:ok])            
            [:baimenik-ez])
          [:baimenik-ez])))))

(defn lortu-bilduma
  "Liburuen bilduma lortzen du."
  [desplazamendua muga db-kon]
  (sql/with-db-connection [kon db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from liburuak"]))
          idak (sql/query kon (orriztatu ["select id from liburuak"] desplazamendua muga))]
      [:ok {:desplazamendua desplazamendua
            :muga muga
            :guztira guztira
            :liburuak (liburuak db-kon idak)}])))

(defn lortu-erabiltzailearenak
  "Erabiltzaile baten liburuen bilduma lortzen du."
  [desp muga db-kon era]
  (sql/with-db-connection [kon db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from liburuak where erabiltzailea=?" era]))
          idak (sql/query kon (orriztatu ["select id from liburuak where erabiltzailea=?" era] desp muga))]
      [:ok {:desplazamendua desp
            :muga muga
            :guztira guztira
            :liburuak (liburuak db-kon idak)}])))

(defn gehitu-gogokoa!
  "Liburua erabiltzailearen gogokoen zerrendan sartzen du."
  [saio-osa db-kon token id]
  (sql/with-db-transaction [kon db-kon]
    (if-let [lib (lortu-liburua kon id)]
      (if-let [era (:erabiltzailea (lortu-saioa saio-osa token))]
        (do (sql/insert! kon :gogokoak
                         [:erabiltzailea :liburua]
                         [era id])    
            [:ok {:gogoko_liburua lib}])        
        [:baimenik-ez])
      [:ezin-prozesatu])))

(defn- lortu-gogokoa [kon era id]
  (if-let [gog (first (sql/query kon ["select erabiltzailea, liburua from gogokoak where erabiltzailea=? and liburua=?" era id]))]
    gog
    nil))

(defn ezabatu-gogokoa!
  "Liburua erabiltzailearen gogokoen zerrendatik kentzen du."
  [saio-osa db-kon token id]
  (sql/with-db-transaction [kon db-kon]
    (if-let [lib (lortu-liburua kon id)]
      (if-let [era (:erabiltzailea (lortu-saioa saio-osa token))]
        (if-let [gog (lortu-gogokoa kon era id)]
          (if (= era (:erabiltzailea gog))
            (do
              (sql/delete! kon :gogokoak ["liburua=?" id])
              [:ok])
            [:baimenik-ez])          
          [:ez-dago])
        [:baimenik-ez])
      [:ezin-prozesatu])))

(defn lortu-gogokoak
  "Erabiltzailearen gogoko liburuak itzultzen ditu"
  [desp muga db-kon era]
  (sql/with-db-connection [kon db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from gogokoak where erabiltzailea=?" era]))
          idak (sql/query kon (orriztatu ["select liburua as id from gogokoak where erabiltzailea=?" era] desp muga))]
      [:ok {:desplazamendua desp
            :muga muga
            :guztira guztira
            :gogoko_liburuak (liburuak db-kon idak)}])))
