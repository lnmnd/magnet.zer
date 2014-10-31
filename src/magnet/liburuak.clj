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

(defn- gogokoak-gehitu
  [kon lib]
  (->>
   (sql/query kon ["select count(liburua) as gogoko_kopurua from gogokoak where liburua=?" (:id lib)])
   first
   :gogoko_kopurua
   (assoc lib :gogoko_kopurua)))

(defn- lortu-liburua [kon id]
  (->> (sql/query kon ["select id, magnet, erabiltzailea, titulua, egileak, hizkuntza, sinopsia, argitaletxea, urtea, generoa, etiketak, azala, igotze_data, iruzkin_kopurua from liburuak where id=?" id])
       first
       eremuak-irakurrita
       (gogokoak-gehitu kon)))

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
                       [:erabiltzailea :magnet :titulua :egileak :hizkuntza :sinopsia :argitaletxea :urtea :generoa :etiketak :azala :igotze_data :iruzkin_kopurua]
                       [(:erabiltzailea edukia) (:magnet edukia) (:titulua edukia) (:egileak edukia) (:hizkuntza edukia)
                        (:sinopsia edukia) (:argitaletxea edukia) (:urtea edukia) (:generoa edukia) (:etiketak edukia)
                        (:azala edukia) (:data edukia) (:iruzkin_kopurua edukia)])
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
      [(liburua-gehitu! (assoc edukia :erabiltzailea erabiltzailea)) 200]
      [{} 401])
    [{} 422]))

(defn lortu
  "Eskatutako id-a duen liburua lortu"
  [id]
  (let [ema (sql/query @konfig/db-kon ["select id, magnet, erabiltzailea, titulua, egileak, hizkuntza, sinopsia, argitaletxea, urtea, generoa, etiketak, azala, igotze_data, iruzkin_kopurua from liburuak where id=?" id])]
    (if (empty? ema)
      [{} 404]
      [{:liburua (eremuak-irakurrita (first ema))} 200])))

(defn aldatu!
  "id bat eta edukia emanda liburua aldatu"
  [token id edukia]
  (if (baliozko-liburu-eskaera edukia)
    (let [[lib egoera] (lortu id)]
      (if (= egoera 404)
        [{} 404]
        (if-let [era (:erabiltzailea (lortu-saioa token))]
          (if (= era (:erabiltzailea (:liburua lib)))
            (liburua-aldatu! id (assoc edukia :erabiltzailea era))
            [{} 401])
          [{} 401])))
    [{} 422]))

(defn ezabatu!
  "id bat emanda liburua ezabatu"
  [token id]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [[lib egoera] (lortu id)]
      (if (= egoera 404)
        [{} 404]
        (if-let [era (:erabiltzailea (lortu-saioa token))]
          (if (= era (:erabiltzailea (:liburua lib)))
            (do (sql/delete! kon :liburuak ["id=?" id])
                [{} 200])            
            [{} 401])
          [{} 401])))))

(defn lortu-bilduma
  "Liburuen bilduma lortzen du."
  [desplazamendua muga]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from liburuak"]))
          liburuak (sql/query kon ["select * from liburuak limit ? offset ?" muga desplazamendua])]
      [{:desplazamendua desplazamendua
        :muga muga
        :guztira guztira
        :liburuak liburuak}
       200])))
