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
                         [:hizkuntza "varchar(255)"]
                         [:sinopsia "varchar(255)"]
                         [:argitaletxea "varchar(255)"]
                         [:urtea "varchar(255)"]
                         [:generoa "varchar(255)"]
                         [:etiketak "varchar(255)"]
                         [:azala "varchar(255)"]
                         [:igotze_data "varchar(255)"]
                         [:iruzkin_kopurua "int"])
   "alter table liburuak add foreign key (erabiltzailea) references erabiltzaileak(erabiltzailea)"
   (sql/create-table-ddl :iruzkinak
                         [:id "bigint auto_increment"]
                         [:liburua "bigint"]
                         [:erabiltzailea "varchar(255)"]
                         [:data "varchar(255)"]
                         [:edukia "varchar(255)"])
   "alter table iruzkinak add foreign key (liburua) references liburuak(id)"
   "alter table iruzkinak add foreign key (erabiltzailea) references erabiltzaileak(erabiltzailea)"
   (sql/create-table-ddl :gogokoak
                         [:erabiltzailea "varchar(255)"]
                         [:liburua "bigint"])
   "alter table gogokoak add foreign key (erabiltzailea) references erabiltzaileak(erabiltzailea)"
   "alter table gogokoak add foreign key (liburua) references liburuak(id)"))

(defn db-garbitu []
  "Taulak ezabatu"
  (sql/db-do-commands @konfig/db-kon
    (sql/drop-table-ddl :erabiltzaileak)
    (sql/drop-table-ddl :liburuak)
    (sql/drop-table-ddl :iruzkinak)
    (sql/drop-table-ddl :gogokoak)))

(defn oraingo-data
  "Oraingo UTC data itzultzen du \"basic-date-time-no-ms\" formatuarekin.
   Formatuak ikusteko: (clj-time.format/show-formatters)"
  []
  (let [formatua (time-format/formatters :basic-date-time-no-ms)
        orain (time/now)]
    (time-format/unparse formatua orain)))

(defmacro trafun
  "Transakzio baten barnean exekutatuko den funtzioa definitzen du."
  [kon izena dok param & gorputza]
  `(defn ~izena ~dok ~param
     (sql/with-db-transaction [~kon @konfig/db-kon]
       ~@gorputza)))
