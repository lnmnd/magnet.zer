(ns magnet.t_erabiltzaileak
  (:use midje.sweet)
  (:require [clj-http.client :as http]
            [clj-json.core :as json]
            [magnet.lagun :refer [db-hasieratu]]))

; Proba guztietarako testuingurua ezartzeko
(background (before :facts
                    (do (println "hasieratu")
                        (def aurrizkia "http://localhost:3000/v1/")
                        (db-hasieratu)
                        ; honekin konfigurazioa aldatu daiteke
                        #_(with-redefs [magnet.konfig/db-con {:classname "org.h2.Driver"
                                                            :subprotocol "h2"
                                                            :subname "jdbc:h2:magnet_test"}]
                         (db-hasieratu)))
                    :after
                    (println "amaitu")))

(fact "Hutsa"
      (let [eran (:body (http/get (str aurrizkia "erabiltzaileak") {:as :json}))]
       eran => []))
