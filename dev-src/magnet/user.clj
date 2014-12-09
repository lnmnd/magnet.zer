(ns magnet.user
  (:require [magnet.zer :as z]
            [magnet.handler :refer [handler-sortu]]
            [magnet.konfiglehenetsia :refer [konfig]]))

(def zer (z/sortu konfig (handler-sortu konfig)))

(defn hasi []
  (z/hasi zer))

(defn geratu []
  (z/geratu zer))
