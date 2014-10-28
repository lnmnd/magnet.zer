(ns magnet.liburuak
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [magnet.lagun :refer [oraingo-data]]
            [magnet.saioak :refer [token-erabiltzailea]]
            [magnet.konfig :as konfig]))

(defn- baliozko-liburu-eskaera
  "Liburuak beharrezko eremu guztiak dituen edo ez"
  [lib]
  (every? #(contains? lib %)
          [:epub :titulua :egileak :sinopsia :urtea :etiketak :azala]))

(defn- eremuak-irakurrita
  "String gisa gordetako eremuak irakurritako liburua"
  [lib]
  (assoc lib :egileak (read-string (:egileak lib))
         :etiketak (read-string (:etiketak lib))))

(defn- lortu-liburua [kon lib]
  (->> (sql/query kon ["select id, magnet, erabiltzailea, titulua, egileak, sinopsia, argitaletxea, urtea, generoa, etiketak, azala, igotze_data, iruzkin_kopurua from liburuak where erabiltzailea=? and titulua=? and egileak=? and sinopsia=? and  urtea=? and  etiketak=?"
                       (:erabiltzailea lib) (:titulua lib) (:egileak lib)
                       (:sinopsia lib) (:urtea lib) (:etiketak lib)])
       first
       eremuak-irakurrita))

(declare lortu)
(defn- liburua-gehitu! [edukia]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [edukia (assoc edukia
                   :argitaletxea (if (nil? (:argitaletxea edukia))
                                   "" (:argitaletxea edukia))
                   :generoa (if (nil? (:generoa edukia))
                              "" (:generoa edukia)))
          egileak (prn-str (:egileak edukia))
          etiketak (prn-str (:etiketak edukia))]
      (do (sql/insert! kon :liburuak
                       [:erabiltzailea :magnet :titulua :egileak :sinopsia :argitaletxea :urtea :generoa :etiketak :azala :igotze_data :iruzkin_kopurua]
                       [(:erabiltzailea edukia) "magnet:?xt=urn:btih:TODO" (:titulua edukia) egileak
                        (:sinopsia edukia) (:argitaletxea edukia) (:urtea edukia) (:generoa edukia) etiketak
                        "TODO-azala-fitxategia-sortu-eta-helbidea-hemen-jarri"
                        (oraingo-data) 0])
          {:liburua (lortu-liburua kon (assoc edukia
                                         :egileak egileak
                                         :etiketak etiketak))}))))

(defn liburua-aldatu! [id edukia]
  (let [argitaletxea (if (nil? (:argitaletxea edukia))
                        "" (:argitaletxea edukia))
        generoa (if (nil? (:generoa edukia))
                  "" (:generoa edukia))]
    (sql/update! @konfig/db-kon :liburuak
                 {:titulua (:titulua edukia)
                  :egileak (prn-str (:egileak edukia))
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
    (if-let [erabiltzailea (token-erabiltzailea token)]
      [(liburua-gehitu! (assoc edukia :erabiltzailea erabiltzailea)) 200]
      [{} 401])
    [{} 422]))

(defn lortu
  "Eskatutako id-a duen liburua lortu"
  [id]
  (let [ema (sql/query @konfig/db-kon ["select id, magnet, erabiltzailea, titulua, egileak, sinopsia, argitaletxea, urtea, generoa, etiketak, azala, igotze_data, iruzkin_kopurua from liburuak where id=?" id])]
    (if (empty? ema)
      [{} 404]
      [{:liburua (eremuak-irakurrita (first ema))} 200])))

(defn aldatu!
  "id bat eta edukia emanda liburua aldatu"
  [token id edukia]
  (if (baliozko-liburu-eskaera edukia)
    (if-let [erabiltzailea (token-erabiltzailea token)]
      (liburua-aldatu! id (assoc edukia :erabiltzailea erabiltzailea))
      [{} 401])
    [{} 422]))

(defn ezabatu!
  "id bat emanda liburua ezabatu"
  [token id]
  (sql/with-db-connection [kon @konfig/db-kon]
    (if (not (empty? (sql/query kon ["select id from liburuak where id=?" id])))
      (if (token-erabiltzailea token)
        (do (sql/delete! kon :liburuak ["id=?" id])
            [{} 200])
        [{} 401])
      [{} 404])))
