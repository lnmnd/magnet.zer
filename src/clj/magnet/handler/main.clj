(ns magnet.handler.main
  (:require [magnet.saioak :refer [sortu-saioak]]
            [magnet.handler :refer [handler-sortu]]
            [magnet.lagun :refer [db-hasieratu]]
            [magnet.zer :refer [sortu hasi]])
  (:gen-class))

(defn hasieratu [konfig]
  (db-hasieratu (:db-kon konfig)))

(defn -main [& [kom]]
  (let [k (eval (read-string (slurp "konfig.clj")))]
      (if (= kom "hasieratu")
        (hasieratu k)
        (hasi (sortu k (handler-sortu k (sortu-saioak)))))))
