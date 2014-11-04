(ns magnet.hainbat
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [magnet.lagun :refer [orriztatu]]
            [magnet.konfig :as konfig]))

(defn- balioak [xs]
  (map (fn [x] (:x x)) xs))

(defn argitaletxeak
  "Argitaletxeen zerrenda itzultzen du."
  [desp muga]
  (sql/with-db-transaction [kon @konfig/db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(distinct argitaletxea) as guztira from liburuak"]))
          xs (sql/query kon (orriztatu ["select distinct argitaletxea as x from liburuak"] desp muga))]
      [:ok {:desplazamendua desp
            :muga muga
            :guztira guztira
            :argitaletxeak (balioak xs)}])))
