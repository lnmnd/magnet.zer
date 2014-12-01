(ns magnet.hainbat
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [magnet.lagun :refer [orriztatu]]
            [magnet.konfig :as konfig]))

(defn- balioak [xs]
  (map (fn [x] (:x x)) xs))

(defn- hainbat-fun [[izena eremua taula]]
  `(defn ~izena [desp# muga#]
     (sql/with-db-transaction [kon# @konfig/db-kon]
       (let [{guztira# :guztira} (first (sql/query kon# [(str "select count(distinct " ~eremua ") as guztira from " ~taula)]))
             xs# (sql/query kon# (orriztatu [(str "select distinct " ~eremua " as x from " ~taula)] desp# muga#))
             gakoa# (keyword (str ~eremua "k"))]
         [:ok {:desplazamendua desp#
               :muga muga#
               :guztira guztira#
               gakoa# (balioak xs#)}]))))

(defmacro ^:private hainbat-erantzunak [& l]
  (cons 'do
        (->> l
             (partition 3)
             (map hainbat-fun))))

(hainbat-erantzunak
 tituluak "titulua" "liburuak"
 egileak "egilea" "liburu_egileak"
 argitaletxeak  "argitaletxea" "liburuak"
 generoak "generoa" "liburuak"
 etiketak "etiketa" "liburu_etiketak"
 urteak "urtea" "liburuak"
 hizkuntzak  "hizkuntza" "liburuak")
