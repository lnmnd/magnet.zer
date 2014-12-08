(ns magnet.handler.main
  (:require [magnet.handler :refer [app]]
            [magnet.zer :refer [sortu hasi]])
  (:gen-class))

(defn -main [& [port]]
  (let [portua (if port (Integer/parseInt port) 3000)]
    (hasi (sortu portua app))))
