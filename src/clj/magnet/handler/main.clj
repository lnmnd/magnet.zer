(ns magnet.handler.main
  (:require [magnet.handler :refer [app]]            
            [magnet.zer :refer [sortu hasi]]
            [magnet.konfiglehenetsia :refer [konfig]])
  (:gen-class))

(defn -main [& [port]]
  (let [portua (if port (Integer/parseInt port) 3000)
        k (if port (assoc konfig :portua (Integer/parseInt port)) konfig)]
    (hasi (sortu k app))))
