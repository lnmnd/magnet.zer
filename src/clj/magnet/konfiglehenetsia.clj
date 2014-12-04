(ns magnet.konfiglehenetsia)

(def db-kon {:classname "org.h2.Driver"
             :subprotocol "h2"
             :subname "jdbc:h2:magnet"})

(def muga 25)

(def saio-iraungitze-denbora (* 60 60)) ; 1 h

(def partekatu true)
(def epub-karpeta "resources/private/torrent/")
(def torrent-karpeta "resources/private/torrent/")
(def irudi-karpeta "resources/public/img/")
(def irudi-url "http://localhost:3000/img/")
