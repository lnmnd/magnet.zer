(ns magnet.erabiltzaileak
  (:require [clojure.java.jdbc :as sql]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [clj-bcrypt-wrapper.core :refer [encrypt gensalt]]
            [magnet.saioak :as saioak]
            [magnet.konfig :as konfig]))

(defn- baliozko-erabiltzailea
  [erabiltzailea]
  (and (and (contains? erabiltzailea :erabiltzailea) (string? (:erabiltzailea erabiltzailea)))
       (and (contains? erabiltzailea :pasahitza) (string? (:pasahitza erabiltzailea)))
       (and (contains? erabiltzailea :izena) (string? (:izena erabiltzailea)))))

(defn- pasahitz-hash
  "Pasahitzaren hash sortzen du bcrypt bidez"
  [pasahitza]
  (encrypt (gensalt 10) pasahitza))

(defn- oraingo-data
  "Oraingo UTC data itzultzen du \"basic-date-time-no-ms\" formatuarekin.
   Formatuak ikusteko: (clj-time.format/show-formatters)"
  []
  (let [formatua (time-format/formatters :basic-date-time-no-ms)
        orain (time/now)]
    (time-format/unparse formatua orain)))

(defn lortu-bilduma [desplazamendua muga]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from erabiltzaileak"]))
          erabiltzaileak (sql/query kon ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak desc limit ? offset ?" muga desplazamendua])]
      [{:desplazamendua desplazamendua
        :muga muga
        :guztira guztira
        :erabiltzaileak erabiltzaileak}
       200])))

(defn lortu [erabiltzailea]
  (let [eran (sql/query @konfig/db-kon ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" erabiltzailea])]
    (if (empty? eran)
      [{} 404]
      [{:erabiltzailea
        (first eran)}
       200])))

(defn gehitu! [edukia]
  (if (baliozko-erabiltzailea edukia)
    (sql/with-db-connection [kon @konfig/db-kon]
      (if (empty? (sql/query kon ["select erabiltzailea from erabiltzaileak where erabiltzailea=?" (:erabiltzailea edukia)]))
        (do (sql/insert! kon :erabiltzaileak
                         [:erabiltzailea :pasahitza :izena :deskribapena :sortze_data]
                         [(:erabiltzailea edukia) (pasahitz-hash (:pasahitza edukia)) (:izena edukia) (:deskribapena edukia) (oraingo-data)])
            [{:erabiltzailea
              (first (sql/query kon ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" (:erabiltzailea edukia)]))}
             200])
        [{} 422]))
    [{} 400]))

(defn aldatu! [token erabiltzailea edukia]
  (if (baliozko-erabiltzailea (assoc edukia :erabiltzailea erabiltzailea))
    (sql/with-db-connection [kon @konfig/db-kon]
      (let [badago (not (empty? (sql/query kon ["select erabiltzailea from erabiltzaileak where erabiltzailea=?" erabiltzailea])))]
        (if badago
          (if (saioak/token-zuzena token erabiltzailea)
            (do
              (sql/update! kon :erabiltzaileak
                           {:pasahitza (pasahitz-hash (:pasahitza edukia))
                            :izena (:izena edukia)
                            :deskribapena (:deskribapena edukia)}
                           ["erabiltzailea=?" erabiltzailea])
              [{:erabiltzailea
                (first (sql/query kon ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" erabiltzailea]))}
               200])
            [{} 401])
          [{} 404])))
    [{} 400]))

(defn ezabatu! [token erabiltzailea]
  (sql/with-db-connection [kon @konfig/db-kon]
    (let [badago (not (empty? (sql/query kon ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" erabiltzailea])))]
      (if badago
        (if (saioak/token-zuzena token erabiltzailea)
          (do (sql/delete! kon :erabiltzaileak ["erabiltzailea=?" erabiltzailea])
              [{} 200])
          [{} 401])
        [{} 404]))))
