(ns ^{:doc "Funtzio laguntzaileak."}
  magnet.lagun
  (:require [clojure.java.jdbc :as sql]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [clj-time.format :as time-format]))

(defn db-hasieratu
  "Datu-basea hasieratzen du"
  [db-kon]
  (sql/db-do-commands
   db-kon
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
                         [:hizkuntza "varchar(255)"]
                         [:sinopsia "varchar(255)"]
                         [:argitaletxea "varchar(255)"]
                         [:urtea "varchar(255)"]
                         [:generoa "varchar(255)"]
                         [:azala "varchar(255)"]
                         [:igotze_data "varchar(255)"])
   "alter table liburuak add foreign key (erabiltzailea) references erabiltzaileak(erabiltzailea) on delete cascade"
   (sql/create-table-ddl :liburu_egileak
                         [:liburua "bigint"]
                         [:egilea "varchar(255)"])
   "alter table liburu_egileak add foreign key (liburua) references liburuak(id) on delete cascade"   
   (sql/create-table-ddl :liburu_etiketak
                         [:liburua "bigint"]
                         [:etiketa "varchar(255)"])
   "alter table liburu_etiketak add foreign key (liburua) references liburuak(id) on delete cascade"
   (sql/create-table-ddl :iruzkinak
                         [:id "bigint auto_increment"]
                         [:liburua "bigint"]
                         [:erabiltzailea "varchar(255)"]
                         [:data "varchar(255)"]
                         [:edukia "varchar(255)"])
   "alter table iruzkinak add foreign key (liburua) references liburuak(id) on delete cascade"
   "alter table iruzkinak add foreign key (erabiltzailea) references erabiltzaileak(erabiltzailea) on delete cascade"
   (sql/create-table-ddl :iruzkin_erantzunak
                         [:gurasoa "bigint"]
                         [:erantzuna "bigint"])
   "alter table iruzkin_erantzunak add foreign key (gurasoa) references iruzkinak(id) on delete cascade"
   "alter table iruzkin_erantzunak add foreign key (erantzuna) references iruzkinak(id) on delete cascade"      
   (sql/create-table-ddl :gogokoak
                         [:erabiltzailea "varchar(255)"]
                         [:liburua "bigint"])
   "alter table gogokoak add foreign key (erabiltzailea) references erabiltzaileak(erabiltzailea) on delete cascade"
   "alter table gogokoak add foreign key (liburua) references liburuak(id) on delete cascade"))

(defmacro ^:private ezabatu-taulak
  [db-kon & taulak]
    (let [aginduak# (map #(sql/drop-table-ddl %) taulak)]
      `(sql/db-do-commands ~db-kon
                           ~@aginduak#)))

(defn db-garbitu [db-kon]
  "Datu-baseko taulak ezabatzen ditu."
  (ezabatu-taulak db-kon :erabiltzaileak :liburuak :liburu_egileak :liburu_etiketak :iruzkinak :iruzkin_erantzunak :gogokoak))

(defn oraingo-data
  "Oraingo UTC data itzultzen du \"date-time-no-ms\" formatuarekin.
   Formatuak ikusteko: (clj-time.format/show-formatters)"
  []
  (let [formatua (time-format/formatters :date-time-no-ms)
        orain (time/now)]
    (time-format/unparse formatua orain)))

(defn segunduak-gehitu
  "\"date-time-no-ms\" formatuko datari segunduak gehitzen dizkio."
  [data seg]
  (let [formatua (time-format/formatters :date-time-no-ms)
        ms (coerce/to-long (time-format/parse formatua data))]
    (->> ms
         (+ (* 1000 seg))
         coerce/from-long
         (time-format/unparse formatua))))

(defn orriztatu
  "Kontsulta, desplazamendua eta muga emanda kontsulta berria sortzen du muga kontutan edukita."
  [kon desp muga]
  (if (= muga 0)
    kon
    (concat [(str (first kon) " limit ? offset ?")]
            (concat (rest kon) [muga desp]))))
