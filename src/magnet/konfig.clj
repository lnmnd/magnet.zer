(ns magnet.konfig)

(def db-kon (atom {:classname "org.h2.Driver"
                   :subprotocol "h2"
                   :subname "jdbc:h2:magnet"}))
