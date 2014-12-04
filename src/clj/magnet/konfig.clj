(ns magnet.konfig
  (:require [magnet.konfiglehenetsia :as l]))

(def db-kon (atom l/db-kon))

; bildumen muga
(def muga (atom l/muga))

; denbora segunduetan
(def saio-iraungitze-denbora (atom l/saio-iraungitze-denbora))

(def partekatu (atom l/partekatu))
(def epub-karpeta (atom l/epub-karpeta))
(def torrent-karpeta (atom l/torrent-karpeta))
(def irudi-karpeta (atom l/irudi-karpeta))
(def irudi-url (atom l/irudi-url))
