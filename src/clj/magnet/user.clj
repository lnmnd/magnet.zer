(ns magnet.user
  (:require [midje.repl :refer :all]
            [magnet.zer :as z]
            [magnet.handler :refer [handler-sortu]]
            [magnet.konfiglehenetsia :refer [konfig]]))

(defonce zer (z/sortu konfig (handler-sortu konfig)))

(defn hasi []
  (z/hasi zer))

(defn geratu []
  (z/geratu zer))
