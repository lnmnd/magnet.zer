(ns magnet.user
  (:require [midje.repl :refer :all]
            [magnet.zer :as z]
            [magnet.saioak :refer [sortu-saioak]]
            [magnet.handler :refer [handler-sortu]]))

(def konfig (eval (read-string (slurp "konfig.clj"))))

(defn sortu []
  (z/sortu konfig (handler-sortu konfig (sortu-saioak))))

(def zer (sortu))

(defn hasi []
  (z/hasi zer))

(defn geratu []
  (z/geratu zer))

(defn berrezarri []
  (geratu)
  (alter-var-root #'zer (constantly (sortu))))
