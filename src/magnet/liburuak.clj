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

(defn eremuak-irakurrita
  "String gisa gordetako eremuak irakurritako liburua"
  [lib]
  (assoc lib :egileak (read-string (:egileak lib))
         :etiketak (read-string (:etiketak lib))))

(defn- lortu-liburua [kon lib]
  (->> (sql/query kon ["select id, magnet, erabiltzailea, titulua, egileak, sinopsia, argitaletxea, urtea, generoa, etiketak, azala, igotze_data, iruzkin_kopurua from liburuak where erabiltzailea=? and titulua=? and egileak=? and sinopsia=? and argitaletxea=? and urtea=? and generoa=? and etiketak=?"
                       (:erabiltzailea lib) (:titulua lib) (:egileak lib)
                       (:sinopsia lib) (:argitaletxea lib) (:urtea lib) (:generoa lib) (:etiketak lib)])
       first
       eremuak-irakurrita))

(defn gehitu! [token edukia]
  (if (baliozko-liburu-eskaera edukia)
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
                         [(token-erabiltzailea token) "magnet:?xt=urn:btih:TODO" (:titulua edukia) egileak
                          (:sinopsia edukia) (:argitaletxea edukia) (:urtea edukia) (:generoa edukia) etiketak
                          "TODO-azala-fitxategia-sortu-eta-helbidea-hemen-jarri"
                          (oraingo-data) 0])
            [{:liburua (lortu-liburua kon (assoc edukia
                                            :erabiltzailea (token-erabiltzailea token)
                                            :egileak egileak
                                            :etiketak etiketak))}
             200])))
    [{} 422]))
