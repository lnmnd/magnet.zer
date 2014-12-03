(ns magnet.torrent
  (:import [java.io File])
  (:import [com.magnet Torrent])
  (:import [java.net InetAddress])
  (:import [com.turn.ttorrent.client SharedTorrent Client]))

(defn sortu!
  "Torrent fitxategia sortu eta horren magnet lotura itzultzen du."
  [epub-fitx torrent-fitx]
  (.lortuMagnetLotura
   (doto (Torrent. (File. epub-fitx))
     (.trackerraGehitu "udp://pi:6969")
     #_(.trackerraGehitu "udp://tracker.publicbt.com:80")
     #_(.trackerraGehitu "udp://tracker.istole.it:6969")
     #_(.trackerraGehitu "udp://tracker.ccc.de:80")
     (.sortu)
     (.gorde (File. torrent-fitx)))))

(defn partekatu!*
  "Torrenta partekatzen du.
   Sarrera gisa java.io.File jasotzen du."
  [torrent katalogoa]
  (let [b (byte-array [0 0 0 0])
        helbidea (InetAddress/getByAddress b)
        torrent (SharedTorrent/fromFile torrent katalogoa)
        bez (Client. helbidea torrent)]
    (.share bez)))

(defn partekatu!
  "Torrenta partekatzen du.
  Sarrera gisa string-ak jasotzen ditu."
  [torrent katalogoa]
  (partekatu!* (File. torrent) (File. katalogoa)))

(defn- torrenta-da?
  [fitx]
  (let [iz (.getName fitx)]
    (not (= -1 (.lastIndexOf iz ".epub.torrent")))))

(defn katalogoko-torrentak-partekatu!
  "Katalogoan dauden torrentak partekatzen ditu."
  [katalogoa]
  (let [dir (File. katalogoa)
        fitxk (.listFiles dir)]
    (doseq [f fitxk]
      (when (torrenta-da? f)
        (partekatu!* f dir)))))

