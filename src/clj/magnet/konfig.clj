(ns magnet.konfig)

(def db-kon-lehenetsia {:classname "org.h2.Driver"
                        :subprotocol "h2"
                        :subname "jdbc:h2:magnet"})
(def db-kon (atom db-kon-lehenetsia))

; bildumen muga
(def muga 25)
