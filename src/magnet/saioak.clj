(ns magnet.saioak
  (:require [clojure.java.jdbc :as sql]
            [clj-bcrypt-wrapper.core :refer [check-password]]
            [magnet.lagun :refer [oraingo-data]]
            [magnet.konfig :as konfig]))

; TODO iraungitze_data pasatzean modu automatikoan kendu
(def ^{:private true} saioak (atom {}))

(defn- gehitu-saioa!
  "Saioa saioen zerrendan sartzen du tokenaren arabera"
  [saioa]
  (swap! saioak conj {(:token saioa) saioa}))

(defn- ausazko-hizkia []
  (rand-nth ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9"
             "A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L" "M" "N" "O" "P" "Q" "R" "S" "T" "U" "V" "W" "X" "Y" "Z"]))

(defn- sortu-tokena
  "Zenbaki eta hizkiz osatutako 32 luzerako tokena sortzen du"
  []
  (->> (for [x (range 32)]
         (ausazko-hizkia))
       (reduce str "")))

(defn- erabiltzaile-zuzena [erabiltzailea pasahitza]
  (if-let [{pasahitz_hash :pasahitza} (first (sql/query @konfig/db-kon ["select pasahitza from erabiltzaileak where erabiltzailea=?" erabiltzailea]))]
    (check-password pasahitza pasahitz_hash)
    false))

(defn hasi!
  "Erabiltzaile baten saioa hasten du"
  [edukia]
  (if (erabiltzaile-zuzena (:erabiltzailea edukia) (:pasahitza edukia))
    (let [saioa {:erabiltzailea (:erabiltzailea edukia)
                 :token (sortu-tokena)
                 :saio_hasiera (oraingo-data)
                 :iraungitze_data "TODO"}]
      (gehitu-saioa! saioa)
      [saioa 200]) 
    [{} 422]))

(defn lortu
  "Tokena duen saioa lortu"
  [token]
  (if (contains? @saioak token)
    [(@saioak token) 200]
    [{} 404]))

(defn amaitu!
  "Saioa amaitu, tokena baliogabetuz"
  [token]
  (swap! saioak dissoc token)
  [{} 200])

(defn token-zuzena
  "true tokena existitzen bada eta erabiltzailearena bada"
  [token erabiltzailea]
  (and (contains? @saioak token)
       (= erabiltzailea (:erabiltzailea (@saioak token)))))

(defn token-erabiltzailea
  "Tokenaren jabea itzultzen du edo false tokena ez badago"
  [token]
  (if (contains? @saioak token)
    (:erabiltzailea (@saioak token))
    false))
