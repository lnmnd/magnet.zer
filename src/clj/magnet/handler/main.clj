(ns magnet.handler.main
  (:require [magnet.zer :refer [sortu hasi]])
  (:gen-class))

(defn -main [& [port]]
  (-> (if port (Integer/parseInt port) 3000)
      sortu
      hasi))
