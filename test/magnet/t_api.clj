(ns magnet.t_api
  (:use midje.sweet)
  (:require [clj-http.client :as http]
            [clj-json.core :as json]
            [magnet.handler.main :refer [zer-hasi zer-geratu]]
            [magnet.lagun :refer [db-hasieratu db-garbitu]]
            [magnet.konfig :as konfig]))

; Probetarako DB konfigurazioa
(def test-kon {:classname "org.h2.Driver"
               :subprotocol "h2"
               :subname "jdbc:h2:test"})

; Proba guztietarako testuingurua ezartzeko
(background (before :facts
                    (do (def aurrizkia "http://localhost:3001/v1/")
                        (zer-hasi 3001)
                        (reset! konfig/db-kon test-kon)
                        (db-hasieratu))
                    :after
                    (do (zer-geratu)
                        (db-garbitu)
                        (reset! konfig/db-kon konfig/db-kon-lehenetsia))))

(defmacro get-json
  "Helbide batetik json edukia lortzeko modu laburragoa"
  [helbidea]
  `(:body (http/get (str aurrizkia ~helbidea) {:as :json})))

(defmacro post-deia
  [helbidea edukia]
  `(http/post (str aurrizkia ~helbidea)
              {:content-type :json
               :body (json/generate-string ~edukia)}))

(defmacro egoera-espero
 [egoera gorputza]
 `(try
    ~gorputza
    (catch Exception e#
      (.getMessage e#) => ~egoera)))

; ERABILTZAILEAK
; --------------
(fact "Hutsa"
      (let [eran (get-json "erabiltzaileak")]
       eran => {:desplazamendua 0
                :muga 10
                :guztira 0
                :erabiltzaileak []})
      (let [eran (http/get (str aurrizkia "erabiltzaileak/ezdago") {:throw-exceptions false})]
        (:status eran) => 404))

(fact "Erabiltzaile okerra"
      ; post-ek salbuespena altxatzen du
      (egoera-espero #"400"
                     (post-deia "erabiltzaileak" {:pasahitza "1234"
                                                  :izena "Era"}))
      (egoera-espero #"400"
                     (post-deia "erabiltzaileak" {:erabiltzailea "era1"
                                                  :izena "Era"}))
      (egoera-espero #"400"
                     (post-deia "erabiltzaileak" {:erabiltzailea "era"
                                                  :pasahitza "1234"})))

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

(fact "Muga aldatu"
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era1"
                  :pasahitza "1234"
                  :izena "era1"})
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era2"
                  :pasahitza "1234"
                  :izena "era2"})
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era3"
                  :pasahitza "1234"
                  :izena "era3"})      
      (let [eran (get-json "erabiltzaileak?muga=2")]
        (:muga eran) => 2
        (:guztira eran) => 3
        (let [era1 (first (:erabiltzaileak eran))]
          (:erabiltzailea era1) => "era1")
        (let [era2 (second (:erabiltzaileak eran))]
          (:erabiltzailea era2) => "era2")))

(fact "Mugaren gehienezko balioa pasa"
      (let [eran (get-json "erabiltzaileak?muga=666")]
        (:muga eran) => 100))

(fact "Desplazamendua gehitu"
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era1"
                  :pasahitza "1234"
                  :izena "era1"})
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era2"
                  :pasahitza "1234"
                  :izena "era2"})
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era3"
                  :pasahitza "1234"
                  :izena "era3"})            
      (let [eran (get-json "erabiltzaileak?desplazamendua=1")]
        (:desplazamendua eran) => 1
        (:guztira eran) => 3
        (count (:erabiltzaileak eran)) => 2
        (let [era2 (first (:erabiltzaileak eran))]
          (:erabiltzailea era2) => "era2")
        (let [era3 (second (:erabiltzaileak eran))]
          (:erabiltzailea era3) => "era3")))

; SAIOAK
; ------
(fact "Saioa hasi"
      (post-deia "erabiltzaileak"
                 {:erabiltzailea "era1"
                  :pasahitza "1234"
                  :izena "Era"})
      (let [saioa (json/parse-string
                  (:body (post-deia "saioak"
                                    {:erabiltzailea "era1"
                                     :pasahitza "1234"}))
                  true)]
        (contains? saioa :erabiltzailea) => true
        (contains? saioa :token) => true
        (contains? saioa :saio_hasiera) => true
        (contains? saioa :iraungitze_data) => true
        (:erabiltzailea saioa) => "era1"
        ; saioa sortu dela egiaztatzeko lortu
        (let [eran (get-json (str "saioak/" (:token saioa)))]
          (:erabiltzailea eran) => "era1")))

; TODO erabiltzailea ez da existitzen
; TODO pasahitz okerra

; ERABILTZAILEAK: token
; ---------------------
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
      (egoera-espero #"404"
                     (http/delete (str aurrizkia "erabiltzaileak/era1"))))

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
