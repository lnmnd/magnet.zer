(ns magnet.zer
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [magnet.torrent :as torrent]
            [magnet.konfig :as konfig]))

(defn sortu
  "Zerbitzaria sortzen du."
  [portua handler]
  {:portua portua
   :handler handler
   :http (atom nil)})

(defn hasi
  "Zerbitzaria abiarazten du."
  [zer]
  (when-not @(:http zer)
    (println ";; Zerbitzaria" (:portua zer) " portuan abiarazten")
    (reset! (:http zer) (run-jetty (:handler zer) {:port (:portua zer) :join? false}))
    (when @konfig/partekatu
      (torrent/katalogoko-torrentak-partekatu! @konfig/torrent-karpeta))))

(defn geratu
  "Zerbitzaria geratzen du."
  [zer]
  (when @(:http zer)
    (println ";; Zerbitzaria geratzen")
    (.stop @(:http zer))
    (reset! (:http zer) nil)))
