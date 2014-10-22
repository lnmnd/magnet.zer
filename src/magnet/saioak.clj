(ns magnet.saioak
  (:require [clojure.java.jdbc :as sql]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [clj-bcrypt-wrapper.core :refer [check-password]]
            [magnet.konfig :as konfig]))

(defn ausazko-hizkia []
  (rand-nth ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9"
             "A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L" "M" "N" "O" "P" "Q" "R" "S" "T" "U" "V" "W" "X" "Y" "Z"]))

(defn sortu-tokena
  "Zenbaki eta hizkiz osatutako 32 luzerako tokena sortzen du"
  []
  (->> (for [x (range 32)]
         (ausazko-hizkia))
       (reduce str "")))

(defn erabiltzaile-zuzena [erabiltzailea pasahitza]
  (let [ema (sql/query @konfig/db-kon ["select pasahitza from erabiltzaileak where erabiltzailea=?" erabiltzailea])]
    (if (empty? ema)
      false
      (let [{pasahitz_hash :pasahitza} (first ema)]
        (check-password pasahitza pasahitz_hash)))))

(defn hasi! [edukia]
  (if (erabiltzaile-zuzena (:erabiltzailea edukia) (:pasahitza edukia))
    [{:erabiltzailea (:erabiltzailea edukia)
      :token (sortu-tokena)
      :saio_hasiera "TODO"
      :iraungitze_data "TODO"}
     200]
    [{} 422]))
