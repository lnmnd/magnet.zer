(ns magnet.konfiglehenetsia
  (:require [clojure.java.shell :refer [sh]]))

(def konfig
  {:portua 3000
   ;; Torrentak partekatu nahi diren edo ez.
   :partekatu true})

(def db-kon {:classname "org.h2.Driver"
             :subprotocol "h2"
             :subname "jdbc:h2:magnet"})

(def muga 25)

(def saio-iraungitze-denbora (* 60 60)) ; 1 h

(def epub-karpeta "resources/private/torrent/")
(def torrent-karpeta "resources/private/torrent/")
(def irudi-karpeta "resources/public/img/")
(def irudi-url "http://localhost:3000/img/")
(def trackerrak ["udp://pi:6969"
                 "udp://tracker.publicbt.com:80"
                 "udp://tracker.istole.it:6969"
                 "udp://tracker.ccc.de:80"])
(def torrent-gehitze-programa (fn [torrent katalogoa]
                                (sh "transmission-remote" "--add" torrent "-w" katalogoa)))
