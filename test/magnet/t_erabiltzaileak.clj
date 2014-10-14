(ns magnet.t_erabiltzaileak
  (:use midje.sweet)
  (:require [clj-http.client :as http]
            [clj-json.core :as json]
            [magnet.handler.main :refer [zer-hasi zer-geratu]]
            [magnet.lagun :refer [db-hasieratu db-garbitu]]))

(def db-con-test {:classname "org.h2.Driver"
                  :subprotocol "h2"
                  :subname "jdbc:h2:magnet_test"})

; Proba guztietarako testuingurua ezartzeko
(with-redefs [magnet.konfig/db-con db-con-test]
  (background (before :facts
                      (do (def aurrizkia "http://localhost:3000/v1/")
                          (zer-hasi 3000)
                          (db-hasieratu))
                      :after
                      (do (zer-geratu)
                          (db-garbitu)))))

(fact "Hutsa"
      (let [eran (:body (http/get (str aurrizkia "erabiltzaileak") {:as :json}))]
       eran => {:desplazamendua 0
                :muga 0
                :guztira 0
                :erabiltzaileak []}))

(fact "Erabiltzaile bat gehitu"
      (let [eran (json/parse-string
                  (:body (http/post (str aurrizkia "erabiltzaileak")
                                    {:content-type :json
                                     :accept :json
                                     :body (json/generate-string {:erabiltzailea "era1"
                                                                  :pasahitza "1234"
                                                                  :izena "Era"
                                                                  :deskribapena "Erabiltzaile bat naiz"})}))
                  true)]
        (let [erab (:erabiltzailea eran)]
          (:erabiltzailea erab) => "era1"
          (:izena erab) => "Era"
          (:deskribapena erab) => "Erabiltzaile bat naiz"))
      (let [eran (:body (http/get (str aurrizkia "erabiltzaileak") {:as :json}))]
        (:guztira eran) => 1
        (let [era1 (first (:erabiltzaileak eran))]
          (:erabiltzailea era1) => "era1"
          (:izena era1) => "Era"
          (:deskribapena era1) => "Erabiltzaile bat naiz"))
      (let [eran (:body (http/get (str aurrizkia "erabiltzaileak/era1") {:as :json}))]
        (let [era1 (:erabiltzailea eran)]
          (:erabiltzailea era1) => "era1"
          (:izena era1) => "Era"
          (:deskribapena era1) => "Erabiltzaile bat naiz")))

(fact "Erabiltzaile batzuk"
      (http/post (str aurrizkia "erabiltzaileak")
                 {:content-type :json
                  :accept :json
                  :body (json/generate-string {:erabiltzailea "era1"
                                               :pasahitza "1234"
                                               :izena "Era"
                                               :deskribapena "Erabiltzaile bat naiz"})})
      (http/post (str aurrizkia "erabiltzaileak")
                 {:content-type :json
                  :accept :json
                  :body (json/generate-string {:erabiltzailea "era2"
                                               :pasahitza "4321"
                                               :izena "Era bi"
                                               :deskribapena "Beste erabiltzaile bat naiz"})})
      (http/post (str aurrizkia "erabiltzaileak")
                 {:content-type :json
                  :accept :json
                  :body (json/generate-string {:erabiltzailea "era3"
                                               :pasahitza "333"
                                               :izena "Era hiru"
                                               :deskribapena "Eta beste bat"})})
      (let [eran (:body (http/get (str aurrizkia "erabiltzaileak") {:as :json}))]
        (:guztira eran) => 3)
      (let [eran (:body (http/get (str aurrizkia "erabiltzaileak/era2") {:as :json}))]
        (let [era2 (:erabiltzailea eran)]
          (:erabiltzailea era2) => "era2"
          (:izena era2) => "Era bi"
          (:deskribapena era2) => "Beste erabiltzaile bat naiz")))
