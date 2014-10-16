(ns magnet.t_erabiltzaileak
  (:use midje.sweet)
  (:require [clj-http.client :as http]
            [clj-json.core :as json]
            [magnet.handler.main :refer [zer-hasi zer-geratu]]
            [magnet.lagun :refer [db-hasieratu db-garbitu]]))

; Proba guztietarako testuingurua ezartzeko
(background (before :facts
                    (do (def aurrizkia "http://localhost:3001/v1/")
                        (zer-hasi 3001)
                        (db-hasieratu))
                    :after
                    (do (zer-geratu)
                        (db-garbitu))))

(defmacro get-json
  "Helbide batetik json edukia lortzeko modu laburragoa"
  [helbidea]
  `(:body (http/get (str aurrizkia ~helbidea) {:as :json})))

(defmacro post-deia
  [helbidea edukia]
  `(http/post (str aurrizkia ~helbidea)
              {:content-type :json
               :body (json/generate-string ~edukia)}))

(fact "Hutsa"
      (let [eran (get-json "erabiltzaileak")]
       eran => {:desplazamendua 0
                :muga 0
                :guztira 0
                :erabiltzaileak []})
      (let [eran (http/get (str aurrizkia "erabiltzaileak/ezdago") {:throw-exceptions false})]
        (:status eran) => 404))

(fact "Erabiltzaile okerra"
      ; post-ek salbuespena altxatzen du
      (try
        (do (post-deia "erabiltzaileak" {:pasahitza "1234"
                                         :izena "Era"})
            false => true)
        (catch Exception e nil))
      (try
        (do (post-deia "erabiltzaileak" {:erabiltzailea "era1"
                                         :izena "Era"})
            false => true)
        (catch Exception e nil))
      (try
        (do (post-deia "erabiltzaileak" {:erabiltzailea "era"
                                         :pasahitza "1234"})
            false => true)
        (catch Exception e nil)))

(fact "Erabiltzaile bat gehitu"
      (let [eran (json/parse-string
                  (:body (post-deia "erabiltzaileak"
                                    {:erabiltzailea "era1"
                                     :pasahitza "1234"
                                     :izena "Era"
                                     :deskribapena "Erabiltzaile bat naiz"}))
                  true)]
        (let [erab (:erabiltzailea eran)]
          (:erabiltzailea erab) => "era1"
          (:izena erab) => "Era"
          (:deskribapena erab) => "Erabiltzaile bat naiz"))
      (let [eran (get-json "erabiltzaileak")]
        (:guztira eran) => 1
        (let [era1 (first (:erabiltzaileak eran))]
          (:erabiltzailea era1) => "era1"
          (:izena era1) => "Era"
          (:deskribapena era1) => "Erabiltzaile bat naiz"))
      (let [eran (get-json "erabiltzaileak/era1")]
        (let [era1 (:erabiltzailea eran)]
          (:erabiltzailea era1) => "era1"
          (:izena era1) => "Era"
          (:deskribapena era1) => "Erabiltzaile bat naiz")))

(fact "Erabiltzaile batzuk"
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era1"
                  :pasahitza "1234"
                  :izena "Era"
                  :deskribapena "Erabiltzaile bat naiz"})
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era2"
                  :pasahitza "4321"
                  :izena "Era bi"
                  :deskribapena "Beste erabiltzaile bat naiz"})
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era3"
                  :pasahitza "333"
                  :izena "Era hiru"
                  :deskribapena "Eta beste bat"})
      (let [eran (get-json "erabiltzaileak")]
        (:guztira eran) => 3)
      (let [eran (get-json "erabiltzaileak/era2")]
        (let [era2 (:erabiltzailea eran)]
          (:erabiltzailea era2) => "era2"
          (:izena era2) => "Era bi"
          (:deskribapena era2) => "Beste erabiltzaile bat naiz")))

; TODO tokena behar da
(fact "Erabiltzaile bat aldatu"
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era1"
                  :pasahitza "1234"
                  :izena "Era"
                  :deskribapena "Erabiltzaile bat naiz"})
      (http/put (str aurrizkia "erabiltzaileak/era1")
                {:content-type :json
                 :accept :json
                 :body (json/generate-string {:pasahitza "1111"
                                              :izena "Era berria"
                                              :deskribapena "Aldatutako erabiltzaile bat naiz"})})
      (let [eran (get-json "erabiltzaileak/era1")]
        (let [era1 (:erabiltzailea eran)]
          (:izena era1) => "Era berria"
          (:deskribapena era1) => "Aldatutako erabiltzaile bat naiz")))

; TODO tokena behar da
(fact "Ez dagoen erabiltzailea ezabatzen saiatu"
      (try
        (do
          (http/delete (str aurrizkia "erabiltzaileak/era1"))
          false => true)
        (catch Exception _ nil)))

; TODO tokena behar da
(fact "Erabiltzaile bat ezabatu"
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era1"
                  :pasahitza "1234"
                  :izena "Era"
                  :deskribapena "Erabiltzaile bat naiz"})
      (http/delete (str aurrizkia "erabiltzaileak/era1"))
      (let [eran (http/get (str aurrizkia "erabiltzaileak/era1") {:throw-exceptions false})]
        (:status eran) => 404))
