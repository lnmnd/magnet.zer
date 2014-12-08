(ns magnet.handler.main
  (:require [magnet.handler :refer [handler-sortu]]            
            [magnet.zer :refer [sortu hasi]]
            [magnet.konfiglehenetsia :refer [konfig]])
  (:gen-class))

(defn -main [& [port]]
  (let [k (if port (assoc konfig :portua (Integer/parseInt port)) konfig)]
    (hasi (sortu k (handler-sortu k)))))
