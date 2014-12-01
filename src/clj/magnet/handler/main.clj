(ns magnet.handler.main
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [magnet.torrent :as torrent]
            [magnet.handler :refer [app]]
            [magnet.konfig :as konfig])
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
  (zer-hasi (if port (Integer/parseInt port) 3000))
  (when @konfig/partekatu
    (torrent/katalogoko-torrentak-partekatu! @konfig/torrent-karpeta)))
