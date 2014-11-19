(ns magnet.torrent
  (:import [java.io File])
  (:import [com.magnet Torrent])
  (:import [java.net InetAddress])
  (:import [com.turn.ttorrent.client SharedTorrent Client]))

(defn sortu!
  "Torrent fitxategia sortu eta horren magnet lotura itzultzen du."
  [epub-fitx torrent-fitx]
  (let [t (Torrent. (File. epub-fitx))]
    (.trackerraGehitu t "udp://tracker.istole.it:6969")
    (.trackerraGehitu t "udp://tracker.ccc.de:80")
    (.sortu t)
    (.gorde t (File. torrent-fitx))
    (.lortuMagnetLotura t)))

(defn partekatu!
  "Torrenta partekatzen du."
  [torrent katalogoa]
  (let [b (byte-array [0 0 0 0])
        helbidea (InetAddress/getByAddress b)
        torrent (SharedTorrent/fromFile (File. torrent) (File. katalogoa))
        bez (Client. helbidea torrent)]
    (.share bez)))

(defn- torrenta-da?
  [fitx]
  (let [iz (.getName fitx)]
    (not (= -1 (.lastIndexOf iz ".epub.torrent")))))

(defn katalogoko-torrentak-partekatu!
  "Katalogoan dauden torrentak partekatzen ditu."
  [katalogoa]
  (let [f (File. katalogoa)
        fitxk (.listFiles f)]
    (doseq [f fitxk]
      (when (torrenta-da? f)
        (partekatu! (str katalogoa (.getName f)) katalogoa)))))
