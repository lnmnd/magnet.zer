(ns magnet.liburuak
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [magnet.lagun :refer [oraingo-data]]
            [magnet.saioak :refer [lortu-saioa]]
            [magnet.konfig :as konfig]))

(defn- baliozko-liburu-eskaera
  "Liburuak beharrezko eremu guztiak dituen edo ez"
  [lib]
  (every? #(contains? lib %)
          [:epub :titulua :egileak :hizkuntza :sinopsia :urtea :etiketak :azala]))

(defn- eremuak-irakurrita
  "String gisa gordetako eremuak irakurritako liburua"
  [lib]
  (assoc lib :egileak (read-string (:egileak lib))
         :etiketak (read-string (:etiketak lib))))

(defn- egileak-gehitu
  [kon lib]
  (if-let [egilea (->> (sql/query kon ["select egilea from liburu_egileak where liburua=?" (:id lib)])
                    first
                    :egilea)]
    (assoc lib :egileak (conj (:egileak egilea)))
    lib))

(defn- iruzkin-kopurua-gehitu
  [kon lib]
  (->>
   (sql/query kon ["select count(liburua) as iruzkin_kopurua from iruzkinak where liburua=?" (:id lib)])
   first
   :iruzkin_kopurua
   (assoc lib :iruzkin_kopurua)))

(defn- gogokoak-gehitu
  [kon lib]
  (->>
   (sql/query kon ["select count(liburua) as gogoko_kopurua from gogokoak where liburua=?" (:id lib)])
   first
   :gogoko_kopurua
   (assoc lib :gogoko_kopurua)))

(defn- lortu-liburua [kon id]
  (first (sql/query kon ["select id, magnet, erabiltzailea, titulua, egileak, hizkuntza, sinopsia, argitaletxea, urtea, generoa, etiketak, azala, igotze_data from liburuak where id=?" id])))

(declare lortu)
(defn- liburua-gehitu! [edukia]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [edukia (assoc edukia
                   :magnet "magnet:?xt=urn:btih:TODO"
                   :argitaletxea (if (nil? (:argitaletxea edukia))
                                   "" (:argitaletxea edukia))
                   :generoa (if (nil? (:generoa edukia))
                              "" (:generoa edukia))
                   :egileak (prn-str (:egileak edukia))
                   :etiketak (prn-str (:etiketak edukia))
                   :azala "TODO-azala-fitxategia-sortu-eta-helbidea-hemen-jarri"
                   :data (oraingo-data)
                   :iruzkin_kopurua 0
                   :gogoko_kopurua 0)]
      (do (sql/insert! kon :liburuak
                       [:erabiltzailea :magnet :titulua :egileak :hizkuntza :sinopsia :argitaletxea :urtea :generoa :etiketak :azala :igotze_data]
                       [(:erabiltzailea edukia) (:magnet edukia) (:titulua edukia) (:egileak edukia) (:hizkuntza edukia)
                        (:sinopsia edukia) (:argitaletxea edukia) (:urtea edukia) (:generoa edukia) (:etiketak edukia)
                        (:azala edukia) (:data edukia)])
          {:liburua (->> (sql/query kon "select identity() as id")
                         first
                         :id
                         (assoc edukia :id)
                         eremuak-irakurrita)}))))

(defn liburua-aldatu! [id edukia]
  (let [argitaletxea (if (nil? (:argitaletxea edukia))
                        "" (:argitaletxea edukia))
        generoa (if (nil? (:generoa edukia))
                  "" (:generoa edukia))]
    (sql/update! @konfig/db-kon :liburuak
                 {:titulua (:titulua edukia)
                  :egileak (prn-str (:egileak edukia))
                  :hizkuntza (:hizkuntza edukia)
                  :sinopsia (:sinopsia edukia)
                  :argitaletxea argitaletxea
                  :urtea (:urtea edukia)
                  :generoa generoa
                  :etiketak (prn-str (:etiketak edukia))
                  :azala (:azala edukia)}
                 ["id=?" id])
    (lortu id)))

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
    [:ok {:liburua (->> lib eremuak-irakurrita
                        (iruzkin-kopurua-gehitu @konfig/db-kon)
                        (gogokoak-gehitu @konfig/db-kon))}]
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
            (liburua-aldatu! id (assoc edukia :erabiltzailea era))
            [:baimenik-ez])
          [:baimenik-ez])))
    [:ezin-prozesatu]))

(defn ezabatu!
  "id bat emanda liburua ezabatu"
  [token id]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [[egoera lib] (lortu id)]
      (if (= egoera :ez-dago)
        [:ez-dago]
        (if-let [era (:erabiltzailea (lortu-saioa token))]
          (if (= era (:erabiltzailea (:liburua lib)))
            (do (sql/delete! kon :liburuak ["id=?" id])
                [:ok])            
            [:baimenik-ez])
          [:baimenik-ez])))))

(defn lortu-bilduma
  "Liburuen bilduma lortzen du."
  [desplazamendua muga]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from liburuak"]))
          liburuak (sql/query kon ["select * from liburuak limit ? offset ?" muga desplazamendua])]
      [:ok {:desplazamendua desplazamendua
            :muga muga
            :guztira guztira
            :liburuak liburuak}])))
