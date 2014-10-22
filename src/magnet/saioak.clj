(ns magnet.saioak
  (:require [clojure.java.jdbc :as sql]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [clj-bcrypt-wrapper.core :refer [check-password]]
            [magnet.konfig :as konfig]))

(defn erabiltzaile-zuzena [erabiltzailea pasahitza]
  (let [ema (sql/query @konfig/db-kon ["select pasahitza from erabiltzaileak where erabiltzailea=?" erabiltzailea])]
    (if (empty? ema)
      false
      (let [{pasahitz_hash :pasahitza} (first ema)]
        (check-password pasahitza pasahitz_hash)))))

(defn hasi! [edukia]
  (if (erabiltzaile-zuzena (:erabiltzailea edukia) (:pasahitza edukia))
    [{:erabiltzailea (:erabiltzailea edukia)
      :token "TODO"
      :saio_hasiera "TODO"
      :iraungitze_data "TODO"}
     200]
    [{} 422]))
