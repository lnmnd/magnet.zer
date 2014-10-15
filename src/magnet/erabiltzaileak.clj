(ns magnet.erabiltzaileak
  (:require [clojure.java.jdbc :as sql]
            [magnet.konfig :as konfig]))

(defn baliozko-erabiltzailea
  [erabiltzailea]
  (and (and (contains? erabiltzailea :erabiltzailea) (string? (:erabiltzailea erabiltzailea)))
       (and (contains? erabiltzailea :pasahitza) (string? (:pasahitza erabiltzailea)))
       (and (contains? erabiltzailea :izena) (string? (:izena erabiltzailea)))))

(defn pasahitz-hash
  "Pasahitzaren hash-a lortzen du. TODO oraingoz ezer ez"
  [pas]
  pas)

(defn lortu-bilduma []
  (let [era (sql/with-connection konfig/db-con
              (sql/with-query-results res
                ["select erabiltzailea, pasahitza, izena, deskribapena, sortze_data from erabiltzaileak"]
                (doall res)))]
    (if era
      (let [erabiltzaileak (sql/with-connection konfig/db-con
                             (sql/with-query-results res
                               ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak desc"]
                               (doall res)))]
        [{:desplazamendua 0
          :muga 0
          :guztira (count erabiltzaileak)
          :erabiltzaileak erabiltzaileak}
         200])
      [{:desplazamendua 0
        :muga 0
        :guztira 0
        :erabiltzaileak []}
       200])))

(defn lortu [erabiltzailea]
  (let [eran (sql/with-connection konfig/db-con
               (sql/with-query-results res
                 ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" erabiltzailea]
                 (doall res)))]
    (if eran
      [{:erabiltzailea
        (first eran)}
       200]
      [{} 404])))

(defn sortu [edukia]
  (if (baliozko-erabiltzailea edukia)
    (do (sql/with-connection konfig/db-con
          (sql/insert-values :erabiltzaileak
                             [:erabiltzailea :pasahitza :izena :deskribapena :sortze_data]
                             [(:erabiltzailea edukia) (pasahitz-hash (:pasahitza edukia)) (:izena edukia) (:deskribapena edukia) "TODO zehazteke"]))
        [{:erabiltzailea
          (first (sql/with-connection konfig/db-con
                   (sql/with-query-results res
                     ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" (:erabiltzailea edukia)]
                     (doall res))))}
         200])
    [{} 400]))

(defn aldatu [erabiltzailea edukia]
  (if (baliozko-erabiltzailea (assoc edukia :erabiltzailea erabiltzailea))
    (do (sql/with-connection konfig/db-con
          (sql/update-values :erabiltzaileak
                             ["erabiltzailea=?" erabiltzailea]
                             {:pasahitza (pasahitz-hash (:pasahitza edukia))
                              :izena (:izena edukia)
                              :deskribapena (:deskribapena edukia)}))
        [{:erabiltzailea
          (first (sql/with-connection konfig/db-con
                   (sql/with-query-results res
                     ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" erabiltzailea]
                     (doall res))))}
         200])
    [{} 400]))
