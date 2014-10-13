(ns magnet.handler.main
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [magnet.handler :refer [app]])
  (:gen-class))

(defn -main [& [port]]
  (run-jetty #'app {:port (if port (Integer/parseInt port) 8080)}))
