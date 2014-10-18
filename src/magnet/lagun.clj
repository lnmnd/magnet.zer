(ns magnet.lagun
  (:require [clojure.java.jdbc :as sql]
            [magnet.konfig :as konfig]))

(defn db-hasieratu
  "Datubasea hasieratzen du"
  []
  (sql/db-do-commands
   konfig/db-con
   (sql/create-table-ddl :erabiltzaileak
                         [:erabiltzailea "varchar(255) primary key"]
                         [:pasahitza "varchar(255)"]
                         [:izena "varchar(255)"]
                         [:deskribapena "varchar(255)"]
                         [:sortze_data "varchar(255)"])))

(defn db-garbitu []
  "Taulak ezabatu"
  (sql/db-do-commands konfig/db-con
    (sql/drop-table-ddl :erabiltzaileak)))
