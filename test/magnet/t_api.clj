(ns magnet.t_api
  (:use midje.sweet)
  (:require [org.httpkit.client :as http]
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

(defn api-deia
  "API deia burutu eta erantzuna jaso eskatzen bada"
  ([metodoa helbidea]
     (api-deia metodoa helbidea :ezer {}))
  ([metodoa helbidea erantzun-mota]
     (api-deia metodoa helbidea erantzun-mota {}))  
  ([metodoa helbidea erantzun-mota gorputza]
     (let [metodoak {:get http/get :post http/post :put http/put :delete http/delete}
           ema @((metodoa metodoak) (str "http://localhost:3001/v1/" helbidea) {:body (json/generate-string gorputza)})]
       (condp = erantzun-mota
         :json
         (json/parse-string (:body ema) true)
         :egoera
         (:status ema)
         :ezer
         nil
         nil))))

(defmacro get-json
  "Helbide batetik json edukia lortzeko modu laburragoa"
  [helbidea]
  `(api-deia :get ~helbidea :json))

; ERABILTZAILEAK
; --------------
(fact "Hutsa" :erabiltzaileak
      (let [eran (get-json "erabiltzaileak")]
       eran => {:desplazamendua 0
                :muga 10
                :guztira 0
                :erabiltzaileak []})
      (let [egoera (api-deia :get (str aurrizkia "erabiltzaileak/ezdago") :egoera)]
        egoera => 404))

(fact "Erabiltzaile okerra" :erabiltzaileak
      (let [egoera (api-deia :post "erabiltzaileak" :egoera
                             {:pasahitza "1234"
                              :izena "Era"})]
        egoera => 400)
      (let [egoera (api-deia :post "erabiltzaileak" :egoera
                             {:erabiltzailea "era1"
                              :izena "Era"})]
        egoera => 400)
      (let [egoera (api-deia :post "erabiltzaileak" :egoera
                             {:erabiltzailea "era"
                              :pasahitza "1234"})]
        egoera => 400))

(fact "Erabiltzaile bat gehitu" :erabiltzaileak
      (let [eran (api-deia :post "erabiltzaileak" :json
                                    {:erabiltzailea "era1"
                                     :pasahitza "1234"
                                     :izena "Era"
                                     :deskribapena "Erabiltzaile bat naiz"})]
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

(fact "Erabiltzaile batzuk" :erabiltzaileak
      (api-deia :post "erabiltzaileak" :ezer
                {:erabiltzailea "era1"
                 :pasahitza "1234"
                 :izena "Era"
                 :deskribapena "Erabiltzaile bat naiz"})
      (api-deia :post "erabiltzaileak" :ezer
                {:erabiltzailea "era2"
                 :pasahitza "4321"
                 :izena "Era bi"
                 :deskribapena "Beste erabiltzaile bat naiz"})
      (api-deia :post "erabiltzaileak" :ezer
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

(fact "Muga aldatu" :erabiltzaileak
      (api-deia :post "erabiltzaileak" :ezer
                 {:erabiltzailea "era1"
                  :pasahitza "1234"
                  :izena "era1"})
      (api-deia :post "erabiltzaileak" :ezer
                 {:erabiltzailea "era2"
                  :pasahitza "1234"
                  :izena "era2"})
      (api-deia :post "erabiltzaileak" :ezer
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

(fact "Mugaren gehienezko balioa pasa" :erabiltzaileak
      (let [eran (get-json "erabiltzaileak?muga=666")]
        (:muga eran) => 100))

(fact "Desplazamendua gehitu" :erabiltzaileak
      (api-deia :post "erabiltzaileak" :ezer
                 {:erabiltzailea "era1"
                  :pasahitza "1234"
                  :izena "era1"})
      (api-deia :post "erabiltzaileak" :ezer
                 {:erabiltzailea "era2"
                  :pasahitza "1234"
                  :izena "era2"})
      (api-deia :post "erabiltzaileak" :ezer
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
(fact "Saioak" :saioak
  (api-deia :post "erabiltzaileak" :ezer
            {:erabiltzailea "era1"
             :pasahitza "1234"
             :izena "Era"})
  (let [saioa (api-deia :post "saioak" :json
                        {:erabiltzailea "era1"
                         :pasahitza "1234"})]
    (contains? saioa :erabiltzailea) => true
    (contains? saioa :token) => true
    (contains? saioa :saio_hasiera) => true
    (contains? saioa :iraungitze_data) => true
    (:erabiltzailea saioa) => "era1"
    ; saioa sortu dela egiaztatzeko lortu
    (let [eran (api-deia :get (str "saioak/" (:token saioa)) :json)]
      (:erabiltzailea eran) => "era1")
    ; saioa amaitu
    (api-deia :delete (str "saioak/" (:token saioa)))
    (let [egoera (api-deia :get (str "saioak/" (:token saioa)) :egoera)]
      egoera => 404)))

; TODO erabiltzailea ez da existitzen
; TODO pasahitz okerra

; ERABILTZAILEAK: token
; ---------------------
; TODO tokena behar da
(fact "Erabiltzaile bat aldatu" :erabiltzaileak :t
      (api-deia :post "erabiltzaileak" :ezer
                {:erabiltzailea "era1"
                 :pasahitza "1234"
                 :izena "Era"
                 :deskribapena "Erabiltzaile bat naiz"})
      (api-deia :put "erabiltzaileak/era1" :egoera
                {:pasahitza "1111"
                 :izena "Era berria"
                 :deskribapena "Aldatutako erabiltzaile bat naiz"})      
      (let [eran (api-deia :get "erabiltzaileak/era1" :json)]
        (let [era1 (:erabiltzailea eran)]
          (:izena era1) => "Era berria"
          (:deskribapena era1) => "Aldatutako erabiltzaile bat naiz")))

; TODO tokena behar da
(fact "Ez dagoen erabiltzailea ezabatzen saiatu" :erabiltzaileak
      (let [egoera (api-deia :delete (str aurrizkia "erabiltzaileak/era1") :egoera)]
        egoera => 404))

; TODO tokena behar da
(fact "Erabiltzaile bat ezabatu" :erabiltzaileak
      (api-deia :post "erabiltzaileak" :ezer
                {:erabiltzailea "era1"
                 :pasahitza "1234"
                 :izena "Era"
                 :deskribapena "Erabiltzaile bat naiz"})
      (api-deia :delete (str aurrizkia "erabiltzaileak/era1"))
      (let [egoera (api-deia :get (str aurrizkia "erabiltzaileak/era1") :egoera)]
        egoera => 404))
