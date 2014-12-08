(ns magnet.zer
  (:require  [org.httpkit.server :refer [run-server]]
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
    (reset! (:http zer) (run-server (:handler zer) {:port (:portua (:konfig zer)) :join? false}))
    (when (:partekatu (:konfig zer))
      (torrent/katalogoko-torrentak-partekatu! (:torrent-gehitze-programa (:konfig zer)) (:torrent-karpeta (:kokapenak (:konfig zer)))))))

(defn geratu
  "Zerbitzaria geratzen du."
  [zer]
  (when @(:http zer)
    (println ";; Zerbitzaria geratzen")
    (@(:http zer) :timeout 1000)
    (reset! (:http zer) nil)))
