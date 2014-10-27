(ns magnet.liburuak
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [magnet.lagun :refer [oraingo-data]]
            [magnet.saioak :refer [token-erabiltzailea]]
            [magnet.konfig :as konfig]))

(defn egileak-lortuta [era]
  "Erabiltzailea egileen zerrenda string-etik aterata"
  (assoc era :egileak (read-string (:egileak era))))

(defn- lortu-liburua [kon lib]
  (->> (sql/query kon ["select id, magnet, erabiltzailea, titulua, egileak from liburuak where erabiltzailea=? and titulua=? and egileak=?" (:erabiltzailea lib) (:titulua lib) (:egileak lib)])
       first
       egileak-lortuta))

(defn gehitu! [token edukia]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [egileak (prn-str (:egileak edukia))]
      (do (sql/insert! kon :liburuak
                       [:erabiltzailea :magnet :titulua :egileak]
                       [(token-erabiltzailea token) "magnet:?xt=urn:btih:TODO" (:titulua edukia) egileak])
          [{:liburua (lortu-liburua kon (assoc edukia
                                          :erabiltzailea (token-erabiltzailea token)
                                          :egileak egileak))}
           200]))))
