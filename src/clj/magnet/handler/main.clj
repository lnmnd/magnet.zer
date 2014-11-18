(ns magnet.handler.main
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [magnet.handler :refer [app]])
  (:gen-class))

(defonce zerbitzaria (atom nil))

(defn zer-hasi
  "Zerbitzaria abiarazi"
  [portua]
  (when-not @zerbitzaria
    (println "Zerbitzaria" portua " portuan abiarazten")
    (swap! zerbitzaria (fn [_] (run-jetty #'app {:port portua :join? false})))))

(defn zer-geratu
  "Zerbitzaria geratu"
  []
  (when @zerbitzaria
    (println "Zerbitzaria geratzen")
    (.stop @zerbitzaria)
    (swap! zerbitzaria (fn [_] nil))))

(defn -main [& [port]]
  (zer-hasi (if port (Integer/parseInt port) 8080)))
