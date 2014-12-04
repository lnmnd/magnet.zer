(ns magnet.konfig)

(def db-kon-lehenetsia {:classname "org.h2.Driver"
                        :subprotocol "h2"
                        :subname "jdbc:h2:magnet"})
(def db-kon (atom db-kon-lehenetsia))

; bildumen muga
(def muga-lehenetsia 25)
(def muga (atom muga-lehenetsia))

; denbora segunduetan
(def saio-iraungitze-denbora-lehenetsia (* 60 60)) ; 1 h
(def saio-iraungitze-denbora (atom saio-iraungitze-denbora-lehenetsia))

(def partekatu-lehenetsia true)
(def partekatu (atom partekatu-lehenetsia))
(def epub-karpeta-lehenetsia "resources/private/torrent/")
(def epub-karpeta (atom epub-karpeta-lehenetsia))
(def torrent-karpeta-lehenetsia "resources/private/torrent/")
(def torrent-karpeta (atom torrent-karpeta-lehenetsia))
(def irudi-karpeta-lehenetsia "resources/public/img/")
(def irudi-karpeta (atom irudi-karpeta-lehenetsia))
(def irudi-url-lehenetsia "http://localhost:3000/img/")
(def irudi-url (atom irudi-url-lehenetsia))
