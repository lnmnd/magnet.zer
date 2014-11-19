(ns magnet.torrent
  (:import [com.magnet Torrent])
  (:import [java.net InetAddress])
  (:import [com.turn.ttorrent.client SharedTorrent Client]))

(defn sortu!
  "Torrent fitxategia sortu eta horren magnet lotura itzultzen du."
  [epub-fitx torrent-fitx]
  (let [t (Torrent. (java.io.File. epub-fitx))]
    (.trackerraGehitu t "udp://tracker.istole.it:6969")
    (.trackerraGehitu t "udp://tracker.ccc.de:80")
    (.sortu t)
    (.gorde t (java.io.File. torrent-fitx))
    (.lortuMagnetLotura t)))

(defn partekatu!
  "Torrenta partekatzen du."
  [torrent katalogoa]
  (let [b (byte-array [0 0 0 0])
        helbidea (InetAddress/getByAddress b)
        torrent (SharedTorrent/fromFile (java.io.File. torrent) (java.io.File. katalogoa))
        bez (Client. helbidea torrent)]
    (.share bez)))
