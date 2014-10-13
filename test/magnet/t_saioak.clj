(ns magnet.t_saioak
  (:use midje.sweet)
  (:require [clj-http.client :as http]
            [clj-json.core :as json]))

; Proba guztietarako testuingurua ezartzeko
(background (before :facts
                    (do (println "hasieratu")
                        (def aurrizkia "http://localhost:3000/v1/"))
                    :after
                    (println "amaitu")))

; Hemen API probak
(fact "proba1"
      (+ 1 2) => 3
      (+ 2 3) => 5)
(fact "proba2"
      (:id {:id 1 :titulua "tit"}) => 1)
(fact "benetako deia"
      (let [eran (:body (http/get (str aurrizkia "saioak/token_bat") {:as :json}))]
        (:erabiltzailea eran) => "erab1"))
