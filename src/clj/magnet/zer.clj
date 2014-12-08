(ns magnet.zer
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [magnet.torrent :as torrent]
            [magnet.konfig :as konfig]))

(defn sortu
  "Zerbitzaria sortzen du."
  [portua handler]
  {:portua portua
   :handler handler
   :jetty (atom nil)})

(defn hasi
  "Zerbitzaria abiarazten du."
  [zer]
  (when-not @(:jetty zer)
    (println "Zerbitzaria" (:portua zer) " portuan abiarazten")
    (swap! (:jetty zer) (fn [_] (run-jetty (:handler zer) {:port (:portua zer) :join? false})))
    (when @konfig/partekatu
      (torrent/katalogoko-torrentak-partekatu! @konfig/torrent-karpeta))))

(defn geratu
  "Zerbitzaria geratzen du."
  [zer]
  (when @(:jetty zer)
    (println "Zerbitzaria geratzen")
    (.stop @(:jetty zer))
    (swap! (:jetty zer) (fn [_] nil))))
