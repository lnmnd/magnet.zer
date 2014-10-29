(ns magnet.iruzkinak
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [magnet.lagun :refer [oraingo-data]]
            [magnet.saioak :refer [lortu-saioa]]
            [magnet.konfig :as konfig]))

(defn- gehitu-iruzkina!
  [edukia]
  (let [ir (assoc edukia :data (oraingo-data))]
    (sql/with-db-connection [kon @konfig/db-kon]
      (do (sql/insert! kon :iruzkinak
                       [:liburua :erabiltzailea :data :edukia]
                       [(:liburua edukia) (:erabiltzailea edukia) (:data edukia) (:edukia edukia)])
          (assoc edukia :id (:id (first (sql/query kon "select identity() as id"))))))))

(defn- lortu-iruzkina
  [id]
  (first (sql/query @konfig/db-kon ["select * from iruzkinak where id=?" id])))

(defn- aldatu-iruzkina!
  [id edukia]
  (sql/update! @konfig/db-kon :iruzkinak
               {:edukia (:edukia edukia)}
               ["id=?" id]))

(defn gehitu!
  "id liburuarekin lotutako iruzkina gehitu."
  [token id edukia]
  (if-let [{erabiltzailea :erabiltzailea} (lortu-saioa token)]
    [{:iruzkina
      (gehitu-iruzkina! (assoc edukia
                          :liburua id
                          :erabiltzailea erabiltzailea))}
     200]
    [{} 401]))

(defn aldatu!
  "Iruzkinaren edukia aldatzen du."
  [token id edukia]
  (if-let [ir (lortu-iruzkina id)]
    (if (lortu-saioa token)
      (do (aldatu-iruzkina! id edukia)
          [{:iruzkina (assoc ir :edukia (:edukia edukia))}
           200])
      [{} 401])
    [{} 404]))
