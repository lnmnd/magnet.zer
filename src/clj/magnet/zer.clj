(ns magnet.zer
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [magnet.torrent :as torrent]))

(defn sortu
  "Zerbitzaria sortzen du."
  [konfig handler]
  {:konfig konfig
   :handler handler
   :http (atom nil)})

(defn hasi
  "Zerbitzaria abiarazten du."
  [zer]
  (when-not @(:http zer)
    (println ";; Zerbitzaria" (:portua (:konfig zer)) " portuan abiarazten")
    (reset! (:http zer) (run-jetty (:handler zer) {:port (:portua (:konfig zer)) :join? false}))
    (when (:partekatu (:konfig zer))
      (torrent/katalogoko-torrentak-partekatu! (:torrent-gehitze-programa (:konfig zer)) (:torrent-karpeta (:kokapenak (:konfig zer)))))))

(defn geratu
  "Zerbitzaria geratzen du."
  [zer]
  (when @(:http zer)
    (println ";; Zerbitzaria geratzen")
    (.stop @(:http zer))
    (reset! (:http zer) nil)))
