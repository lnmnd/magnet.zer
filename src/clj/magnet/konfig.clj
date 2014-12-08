(ns magnet.konfig
  (:require [magnet.konfiglehenetsia :as l]))

(def ^{:doc "Datubasearen konexio parametroak, dbspec gisa.
  Adb:
  {:classname \"org.h2.Driver
   :subprotocol \"h2\"
   :subname \"jdbc:h2:magnet\"}"}
  db-kon (atom l/db-kon))

(def ^{:doc "Bilduma batean agertuko diren elementu kopurua."}
  muga (atom l/muga))
