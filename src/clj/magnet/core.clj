(ns ^{:doc "Abiapuntua."}
  magnet.core
  (:require [magnet.saioak :refer [saioak-sortu]]
            [magnet.handler :refer [handler-sortu]]
            [magnet.lagun :refer [db-hasieratu]]
            [magnet.zer :as z])
  (:gen-class))

(def konfig (eval (read-string (slurp "konfig.clj"))))

(defn sortu []
  (z/sortu konfig (handler-sortu konfig (saioak-sortu))))

(def zer (sortu))

(defn hasi []
  (z/hasi zer))

(defn geratu []
  (z/geratu zer))

(defn berrezarri []
  (geratu)
  (alter-var-root #'zer (constantly (sortu))))

(defn hasieratu
  "Datu-basea hasieratzen du."
  [konfig]
  (db-hasieratu (:db-kon konfig)))

(defn -main [& [kom]]
  (if (= kom "hasieratu")
    (hasieratu konfig)
    (do (sortu)
        (hasi))))
