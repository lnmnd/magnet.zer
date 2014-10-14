(ns magnet.t_erabiltzaileak
  (:use midje.sweet)
  (:require [clj-http.client :as http]
            [clj-json.core :as json]
            [magnet.lagun :refer [db-hasieratu]]))

; Proba guztietarako testuingurua ezartzeko
(background (before :facts
                    (do (def aurrizkia "http://localhost:3000/v1/")
                        (db-hasieratu))))

(fact "Hutsa"
      (let [eran (:body (http/get (str aurrizkia "erabiltzaileak") {:as :json}))]
       eran => {:desplazamendua 0
                :muga 0
                :guztira 0
                :erabiltzaileak []}))
