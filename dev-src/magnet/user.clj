(ns magnet.user
  (:require [midje.repl :refer :all]
            [magnet.zer :as z]
            [magnet.saioak :refer [sortu-saioak]]
            [magnet.handler :refer [handler-sortu]]
            [magnet.konfiglehenetsia :refer [konfig]]))

(def zer (z/sortu konfig (handler-sortu konfig (sortu-saioak))))

(defn hasi []
  (z/hasi zer))

(defn geratu []
  (z/geratu zer))
