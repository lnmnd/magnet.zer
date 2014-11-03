(ns magnet.erabiltzaileak
  (:require [clojure.java.jdbc :as sql]
            [clj-bcrypt-wrapper.core :refer [encrypt gensalt]]
            [magnet.saioak :refer [lortu-saioa]]
            [magnet.lagun :refer [oraingo-data]]
            [magnet.konfig :as konfig]))

(defn- baliozko-erabiltzailea?
  "Erabiltzaileak beharrezko eremu guztiak dituen edo ez"
  [erabiltzailea]
  (every? #(contains? erabiltzailea %)
          [:erabiltzailea :pasahitza :izena]))

(defn- pasahitz-hash
  "Pasahitzaren hash sortzen du bcrypt bidez"
  [pasahitza]
  (encrypt (gensalt 10) pasahitza))

(defn- gehitu-erabiltzailea! [kon edukia]
  (sql/insert! kon :erabiltzaileak
               [:erabiltzailea :pasahitza :izena :deskribapena :sortze_data]
               [(:erabiltzailea edukia) (pasahitz-hash (:pasahitza edukia)) (:izena edukia) (:deskribapena edukia) (:sotze_data edukia)]))

(defn- badago? [kon era]
  (not (empty? (sql/query kon ["select erabiltzailea from erabiltzaileak where erabiltzailea=?" era]))))

(defn- lortu-erabiltzailea
  "Erabiltzailea lortzen du, nil ez badago"
  [kon erabiltzailea]
  (first (sql/query kon ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" erabiltzailea])))

(defn- aldatu-erabiltzailea!
  "Erabiltzailea aldatzen du."
  [kon erabiltzailea edukia]
  (sql/update! kon :erabiltzaileak
               {:pasahitza (pasahitz-hash (:pasahitza edukia))
                :izena (:izena edukia)
                :deskribapena (:deskribapena edukia)}
               ["erabiltzailea=?" erabiltzailea]))

(defn lortu-bilduma [desplazamendua muga]
  (sql/with-db-transaction [kon @konfig/db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from erabiltzaileak"]))
          erabiltzaileak (sql/query kon ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak desc limit ? offset ?" muga desplazamendua])]
      [:ok {:desplazamendua desplazamendua
            :muga muga
            :guztira guztira
            :erabiltzaileak erabiltzaileak}])))

(defn lortu [erabiltzailea]
  (sql/with-db-transaction [kon @konfig/db-kon]
    (if-let [era (lortu-erabiltzailea @konfig/db-kon erabiltzailea)]
      [:ok {:erabiltzailea era}]
      [:ez-dago])))

(defn gehitu! [edukia]
  (let [edukia (assoc edukia :sortze_data (oraingo-data))]
    (if (baliozko-erabiltzailea? edukia)
      (sql/with-db-transaction [kon @konfig/db-kon]
        (if (badago? kon (:erabiltzailea edukia))
          [:ezin-prozesatu]
          (do (gehitu-erabiltzailea! kon edukia)
              [:ok {:erabiltzailea (dissoc edukia :pasahitza)}])))
      [:ezin-prozesatu])))

(defn aldatu! [token erabiltzailea edukia]
  (if (baliozko-erabiltzailea? (assoc edukia :erabiltzailea erabiltzailea))
    (sql/with-db-transaction [kon @konfig/db-kon]
      (if (badago? kon erabiltzailea)
        (if (= (:erabiltzailea (lortu-saioa token))
               erabiltzailea)
          (do (aldatu-erabiltzailea! kon erabiltzailea edukia)
              [:ok {:erabiltzailea (dissoc edukia :pasahitza)}])
          [:baimenik-ez])
        [:ez-dago]))
    [400]))

(defn ezabatu!
  "Erabiltzaile bat ezabatzen du"
  [token erabiltzailea]
  (sql/with-db-transaction [kon @konfig/db-kon]
    (if (badago? kon erabiltzailea)
      (if (= (:erabiltzailea (lortu-saioa token))
               erabiltzailea)
        (do (sql/delete! kon :erabiltzaileak ["erabiltzailea=?" erabiltzailea])
            [:ok])
        [:baimenik-ez])
      [:ez-dago])))
