(ns magnet.konfig)

(def db-kon-lehenetsia {:classname "org.h2.Driver"
                        :subprotocol "h2"
                        :subname "jdbc:h2:magnet"})
(def db-kon (atom db-kon-lehenetsia))

; bildumen muga
(def muga 25)

(def partekatu-lehenetsia true)
(def partekatu (atom partekatu-lehentsia))
(def epub-karpeta-lehenetsia "resources/private/torrent/")
(def epub-karpeta (atom epub-karpeta-lehenetsia))
(def torrent-karpeta-lehenetsia "resources/private/torrent/")
(def torrent-karpeta (atom torrent-karpeta-lehenetsia))
(def irudi-karpeta-lehenetsia "resources/public/img/")
(def irudi-karpeta (atom irudi-karpeta-lehenetsia))
(def irudi-url-lehenetsia "http://localhost:3000/img/")
(def irudi-url (atom irudi-url-lehenetsia))
