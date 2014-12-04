(ns magnet.torrent
  (:require [magnet.konfig :as konfig])
  (:import [java.io File])
  (:import [com.magnet Torrent]))

(defn sortu!
  "Torrent fitxategia sortu eta horren magnet lotura itzultzen du."
  [epub-fitx torrent-fitx]
  (let [tor (Torrent. (File. epub-fitx))]
    (doseq [tra @konfig/trackerrak]
      (.trackerraGehitu tor tra))
    (.sortu tor)
    (.gorde tor (File. torrent-fitx))
    (.lortuMagnetLotura tor)))

(defn partekatu!
  "Torrenta partekatzen du.
  Sarrera gisa string-ak jasotzen ditu."
  [torrent katalogoa]
  (let [path (str (.getCanonicalPath (File. ".")) "/")]
    (@konfig/torrent-gehitze-programa (str path torrent) (str path katalogoa))))

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

