(ns magnet.handler.main
  (:require [magnet.saioak :refer [sortu-saioak]]
            [magnet.handler :refer [handler-sortu]]            
            [magnet.zer :refer [sortu hasi]])
  (:gen-class))

(defn -main [& []]
  (let [k (eval (read-string (slurp "konfig.clj")))]
    (hasi (sortu k (handler-sortu k (sortu-saioak))))))
