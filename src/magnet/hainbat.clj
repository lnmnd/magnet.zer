(ns magnet.hainbat
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [magnet.lagun :refer [orriztatu]]
            [magnet.konfig :as konfig]))

(defn- balioak [xs]
  (map (fn [x] (:x x)) xs))

(defn- datuak
  "Taulatik eremuaren balio ezberdinak lortzen ditu."
  [eran desp muga eremua taula]
  (sql/with-db-transaction [kon @konfig/db-kon]
    (let [{guztira :guztira} (first (sql/query kon [(str "select count(distinct " eremua ") as guztira from " taula)]))
          xs (sql/query kon (orriztatu [(str "select distinct " eremua " as x from " taula)] desp muga))]
      [:ok {:desplazamendua desp
            :muga muga
            :guztira guztira
            eran (balioak xs)}])))

(defn egileak
  "Egileen zerrenda itzultzen du."
  [desp muga]
  (datuak :egileak desp muga "egilea" "liburu_egileak"))

(defn argitaletxeak
  "Argitaletxeen zerrenda itzultzen du."
  [desp muga]
  (datuak :argitaletxeak desp muga "argitaletxea" "liburuak"))
