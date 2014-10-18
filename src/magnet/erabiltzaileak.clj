(ns magnet.erabiltzaileak
  (:require [clojure.java.jdbc :as sql]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [clj-bcrypt-wrapper.core :refer [encrypt gensalt]]
            [magnet.konfig :as konfig]))

(defn baliozko-erabiltzailea
  [erabiltzailea]
  (and (and (contains? erabiltzailea :erabiltzailea) (string? (:erabiltzailea erabiltzailea)))
       (and (contains? erabiltzailea :pasahitza) (string? (:pasahitza erabiltzailea)))
       (and (contains? erabiltzailea :izena) (string? (:izena erabiltzailea)))))

(defn pasahitz-hash
  "Pasahitzaren hash sortzen du bcrypt bidez"
  [pasahitza]
  (encrypt (gensalt 10) pasahitza))

(defn oraingo-data
  "Oraingo data itzultzen du yyyy-MM-dd formatuarekin"
  []
  (let [formatua (time-format/formatter "yyyy-MM-dd")
        orain (time/now)]
    (time-format/unparse formatua orain)))

(defn lortu-bilduma []
  (let [erabiltzaileak (sql/query konfig/db-con ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak desc"])]
    [{:desplazamendua 0
      :muga 0
      :guztira (count erabiltzaileak)
      :erabiltzaileak erabiltzaileak}
     200]))

(defn lortu [erabiltzailea]
  (let [eran (sql/query konfig/db-con ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" erabiltzailea])]
    (if (empty? eran)
      [{} 404]
      [{:erabiltzailea
        (first eran)}
       200])))

(defn gehitu! [edukia]
  (if (baliozko-erabiltzailea edukia)
    (do (sql/insert! konfig/db-con :erabiltzaileak
                     [:erabiltzailea :pasahitza :izena :deskribapena :sortze_data]
                     [(:erabiltzailea edukia) (pasahitz-hash (:pasahitza edukia)) (:izena edukia) (:deskribapena edukia) (oraingo-data)])
        [{:erabiltzailea
          (first (sql/query konfig/db-con ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" (:erabiltzailea edukia)]))}
         200])
    [{} 400]))

(defn aldatu! [erabiltzailea edukia]
  (if (baliozko-erabiltzailea (assoc edukia :erabiltzailea erabiltzailea))
    (do (sql/update! konfig/db-con :erabiltzaileak
                     {:pasahitza (pasahitz-hash (:pasahitza edukia))
                      :izena (:izena edukia)
                      :deskribapena (:deskribapena edukia)}
                     ["erabiltzailea=?" erabiltzailea])
        [{:erabiltzailea
          (first (sql/query konfig/db-con ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" erabiltzailea]))}
         200])
    [{} 400]))

(defn ezabatu! [erabiltzailea]
  (let [badago (not (empty? (sql/query konfig/db-con ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" erabiltzailea])))]
    (if badago
      (do (sql/delete! konfig/db-con :erabiltzaileak ["erabiltzailea=?" erabiltzailea])
          [{} 200])
      [{} 404])))
