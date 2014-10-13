(ns magnet.lagun
  (:require [clojure.java.jdbc :as sql]
            [magnet.konfig :as konfig]))

(defn db-hasieratu
  "Datubasea hasieratzen du"
  []
  (sql/with-connection konfig/db-con
    (sql/drop-table :erabiltzaileak)
    (sql/create-table :erabiltzaileak
                      [:erabiltzailea "varchar(255) primary key"]
                      [:pasahitza "varchar(255)"]
                      [:izena "varchar(255)"]
                      [:deskribapena "varchar(255)"]
                      ; TODO formatu egokia
                      [:sortze_data "varchar(255)"])))
