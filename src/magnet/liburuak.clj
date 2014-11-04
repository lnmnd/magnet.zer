(ns magnet.liburuak
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [magnet.lagun :refer [oraingo-data orriztatu]]
            [magnet.saioak :refer [lortu-saioa]]
            [magnet.konfig :as konfig]))

(defn- baliozko-liburu-eskaera
  "Liburuak beharrezko eremu guztiak dituen edo ez"
  [lib]
  (every? #(contains? lib %)
          [:epub :titulua :egileak :hizkuntza :sinopsia :urtea :etiketak :azala]))

(defn- egileak [kon id]
  (map (fn [x] (:egilea x))
       (sql/query kon ["select egilea from liburu_egileak where liburua=?" id])))

(defn- etiketak [kon id]
  (map (fn [x] (:etiketa x))
       (sql/query kon ["select etiketa from liburu_etiketak where liburua=?" id])))

(defn- iruzkin-kopurua [kon id]
  (->>
   (sql/query kon ["select count(liburua) as iruzkin_kopurua from iruzkinak where liburua=?" id])
   first
   :iruzkin_kopurua))

(defn- gogokoak [kon id]
  (->>
   (sql/query kon ["select count(liburua) as gogoko_kopurua from gogokoak where liburua=?" id])
   first
   :gogoko_kopurua))

(defn- lortu-liburua [kon id]
  (if-let [lib  (first (sql/query kon ["select id, magnet, erabiltzailea, titulua, hizkuntza, sinopsia, argitaletxea, urtea, generoa, azala, igotze_data from liburuak where id=?" id]))]
    (assoc lib
      :egileak (egileak kon id)
      :etiketak (etiketak kon id)      
      :iruzkin_kopurua (iruzkin-kopurua kon id)
      :gogoko_kopurua (gogokoak kon id))
    nil))

(defn- liburuak [idak]
  (map (fn [x] (lortu-liburua @konfig/db-kon (:id x))) idak))

(defn- liburua-gehitu! [edukia]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [edukia (assoc edukia
                   :magnet "magnet:?xt=urn:btih:TODO"
                   :argitaletxea (if (nil? (:argitaletxea edukia))
                                   "" (:argitaletxea edukia))
                   :generoa (if (nil? (:generoa edukia))
                              "" (:generoa edukia))
                   :azala "TODO-azala-fitxategia-sortu-eta-helbidea-hemen-jarri"
                   :data (oraingo-data)
                   :iruzkin_kopurua 0
                   :gogoko_kopurua 0)]
      (do (sql/insert! kon :liburuak
                       [:erabiltzailea :magnet :titulua :hizkuntza :sinopsia :argitaletxea :urtea :generoa :azala :igotze_data]
                       [(:erabiltzailea edukia) (:magnet edukia) (:titulua edukia) (:hizkuntza edukia)
                        (:sinopsia edukia) (:argitaletxea edukia) (:urtea edukia) (:generoa edukia)
                        (:azala edukia) (:data edukia)])
          (let [id (:id (first (sql/query kon "select identity() as id")))]
            (doseq [egi (:egileak edukia)]
              (sql/insert! kon :liburu_egileak
                           [:liburua :egilea]
                           [id egi]))
            (doseq [eti (:etiketak edukia)]
              (sql/insert! kon :liburu_etiketak
                           [:liburua :etiketa]
                           [id eti]))
            {:liburua (assoc edukia :id id)})))))

(defn liburua-aldatu! [id edukia]
  (let [argitaletxea (if (nil? (:argitaletxea edukia))
                        "" (:argitaletxea edukia))
        generoa (if (nil? (:generoa edukia))
                  "" (:generoa edukia))]
    (sql/update! @konfig/db-kon :liburuak
                 {:titulua (:titulua edukia)
                  :hizkuntza (:hizkuntza edukia)
                  :sinopsia (:sinopsia edukia)
                  :argitaletxea argitaletxea
                  :urtea (:urtea edukia)
                  :generoa generoa
                  :azala (:azala edukia)}
                 ["id=?" id])
    (lortu-liburua @konfig/db-kon id)))

(defn gehitu! [token edukia]
  (if (baliozko-liburu-eskaera edukia)
    (if-let [{erabiltzailea :erabiltzailea} (lortu-saioa token)]
      [:ok (liburua-gehitu! (assoc edukia :erabiltzailea erabiltzailea))]
      [:baimenik-ez])
    [:ezin-prozesatu]))

(defn lortu
  "Eskatutako id-a duen liburua lortu"
  [id]
  (if-let [lib (lortu-liburua @konfig/db-kon id)]
    [:ok {:liburua lib}]
    [:ez-dago]))

(defn aldatu!
  "id bat eta edukia emanda liburua aldatu"
  [token id edukia]
  (if (baliozko-liburu-eskaera edukia)
    (let [[egoera lib] (lortu id)]
      (if (= egoera :ez-dago)
        [:ez-dago]
        (if-let [era (:erabiltzailea (lortu-saioa token))]
          (if (= era (:erabiltzailea (:liburua lib)))
            [:ok {:liburua (liburua-aldatu! id (assoc edukia :erabiltzailea era))}] 
            [:baimenik-ez])
          [:baimenik-ez])))
    [:ezin-prozesatu]))

(defn ezabatu!
  "id bat emanda liburua ezabatu"
  [token id]
  (sql/with-db-transaction [kon @konfig/db-kon]
    (let [[egoera lib] (lortu id)]
      (if (= egoera :ez-dago)
        [:ez-dago]
        (if-let [era (:erabiltzailea (lortu-saioa token))]
          (if (= era (:erabiltzailea (:liburua lib)))
            (do (sql/delete! kon :iruzkinak ["liburua=?" id])
                ; TODO iruzkin_erantzunak
                (sql/delete! kon :liburu_egileak ["liburua=?" id])
                (sql/delete! kon :liburu_etiketak ["liburua=?" id])                
                (sql/delete! kon :liburuak ["id=?" id])
                [:ok])            
            [:baimenik-ez])
          [:baimenik-ez])))))

(defn lortu-bilduma
  "Liburuen bilduma lortzen du."
  [desplazamendua muga]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from liburuak"]))
          idak (sql/query kon (orriztatu ["select id from liburuak"] desplazamendua muga))]
      [:ok {:desplazamendua desplazamendua
            :muga muga
            :guztira guztira
            :liburuak (liburuak idak)}])))

(defn lortu-erabiltzailearenak
  "Erabiltzaile baten liburuen bilduma lortzen du."
  [desp muga era]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from liburuak where erabiltzailea=?" era]))
          idak (sql/query kon (orriztatu ["select id from liburuak where erabiltzailea=?" era] desp muga))]
      [:ok {:desplazamendua desp
            :muga muga
            :guztira guztira
            :liburuak (liburuak idak)}])))

(defn gehitu-gogokoa!
  "Liburua erabiltzailearen gogokoen zerrendan sartzen du."
  [era id]
  (sql/with-db-connection [kon @konfig/db-kon]
    (if-let [lib (lortu-liburua kon id)]
      (do
        (sql/insert! kon :gogokoak
                     [:erabiltzailea :liburua]
                     [era id])    
        [:ok {:gogoko_liburua lib}])
      [:ezin-prozesatu])))
