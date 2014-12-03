(ns magnet.torrent
  (:require [clojure.java.shell :refer [sh]])
  (:import [java.io File])
  (:import [com.magnet Torrent]))

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

(defn partekatu!
  "Torrenta partekatzen du.
  Sarrera gisa string-ak jasotzen ditu."
  [torrent katalogoa]
  (let [path (str (.getCanonicalPath (java.io.File. ".")) "/")]
    (sh "transmission-remote" "--add" (str path torrent) "-w" (str path katalogoa))))

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
        (partekatu! (.getPath f) (.getPath dir))))))

