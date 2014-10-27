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
                         [:sortze_data "varchar(255)"])
   (sql/create-table-ddl :liburuak
                         [:id "bigint auto_increment"]
                         [:erabiltzailea "varchar(255)"]
                         [:magnet "varchar(255)"]
                         [:titulua "varchar(255)"]
                         [:egileak "varchar(255)"]
                         [:sinopsia "varchar(255)"]
                         [:argitaletxea "varchar(255)"]
                         [:urtea "varchar(255)"]
                         [:generoa "varchar(255)"]
                         [:etiketak "varchar(255)"]
                         [:azala "varchar(255)"]
                         [:igotze_data "varchar(255)"]
                         [:iruzkin_kopurua "int"])
   "alter table liburuak add foreign key (erabiltzailea) references erabiltzaileak(erabiltzailea)"))

(defn db-garbitu []
  "Taulak ezabatu"
  (sql/db-do-commands @konfig/db-kon
    (sql/drop-table-ddl :erabiltzaileak)
    (sql/drop-table-ddl :liburuak)))

(defn oraingo-data
  "Oraingo UTC data itzultzen du \"basic-date-time-no-ms\" formatuarekin.
   Formatuak ikusteko: (clj-time.format/show-formatters)"
  []
  (let [formatua (time-format/formatters :basic-date-time-no-ms)
        orain (time/now)]
    (time-format/unparse formatua orain)))
