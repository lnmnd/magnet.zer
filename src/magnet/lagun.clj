(ns magnet.lagun
  (:require [clojure.java.jdbc :as sql]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [magnet.konfig :as konfig]))

(defn db-hasieratu
  "Datubasea hasieratzen du"
  []
  (sql/db-do-commands
   @konfig/db-kon
   (sql/create-table-ddl :erabiltzaileak
                         [:erabiltzailea "varchar(255) primary key"]
                         [:pasahitza "varchar(255)"]
                         [:izena "varchar(255)"]
                         [:deskribapena "varchar(255)"]
                         [:sortze_data "varchar(255)"])))

(defn db-garbitu []
  "Taulak ezabatu"
  (sql/db-do-commands @konfig/db-kon
    (sql/drop-table-ddl :erabiltzaileak)))

(defn oraingo-data
  "Oraingo UTC data itzultzen du \"basic-date-time-no-ms\" formatuarekin.
   Formatuak ikusteko: (clj-time.format/show-formatters)"
  []
  (let [formatua (time-format/formatters :basic-date-time-no-ms)
        orain (time/now)]
    (time-format/unparse formatua orain)))
