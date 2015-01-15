(ns ^{:doc "Abiapuntua."}
  magnet.core
  (:require [magnet.saioak :refer [saioak-sortu]]
            [magnet.handler :refer [handler-sortu]]
            [magnet.lagun :refer [db-hasieratu]]
            [magnet.zer :as z])
  (:gen-class))

(def konfig nil)
(def zer nil)

(defn sortu []
  (alter-var-root #'konfig
                  (constantly (eval (read-string (slurp "konfig.clj")))))
  (alter-var-root #'zer
                  (constantly (z/sortu konfig (handler-sortu konfig (saioak-sortu))))))

(defn hasi []
  (z/hasi zer))

(defn geratu []
  (z/geratu zer))

(defn berrabiarazi []
  (geratu)
  (sortu)
  (hasi))

(defn hasieratu
  "Datu-basea hasieratzen du."
  [konfig]
  (db-hasieratu (:db-kon konfig)))

(defn -main [& [kom]]
  (if (= kom "hasieratu")
    (hasieratu (eval (read-string (slurp "konfig.clj"))))
    (do (sortu)
        (hasi))))
