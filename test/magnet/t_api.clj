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
                    (do (zer-hasi 3001)
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

(defn erabiltzailea-ez-dago
  [erabiltzailea]
  (= 404 (api-deia :get (str "erabiltzaileak/" erabiltzailea) :egoera)))

(defn saioa-hasi
  "Erabiltzailearen saioa hasi. Erabiltzailea existitzen ez bada sortu."
  ([erabiltzailea pasahitza]
     (saioa-hasi erabiltzailea pasahitza "izena"))
  ([erabiltzailea pasahitza izena]
     (when (erabiltzailea-ez-dago erabiltzailea)
       (api-deia :post "erabiltzaileak" :ezer
                 {:erabiltzailea erabiltzailea
                  :pasahitza pasahitza
                  :izena izena}))
     (:token (api-deia :post "saioak" :json
                       {:erabiltzailea erabiltzailea
                        :pasahitza pasahitza}))))

; ERABILTZAILEAK
; --------------
(fact "Hutsa" :erabiltzaileak
      (let [eran (get-json "erabiltzaileak")]
       eran => {:desplazamendua 0
                :muga 25
                :guztira 0
                :erabiltzaileak []})
      (let [egoera (api-deia :get "erabiltzaileak/ezdago" :egoera)]
        egoera => 404))

(fact "Erabiltzaile okerra" :erabiltzaileak
      (let [egoera (api-deia :post "erabiltzaileak" :egoera
                             {:pasahitza "1234"
                              :izena "Era"})]
        egoera => 422)
      (let [egoera (api-deia :post "erabiltzaileak" :egoera
                             {:erabiltzailea "era1"
                              :izena "Era"})]
        egoera => 422)
      (let [egoera (api-deia :post "erabiltzaileak" :egoera
                             {:erabiltzailea "era"
                              :pasahitza "1234"})]
        egoera => 422))

(fact "Erabiltzaile bat gehitu" :erabiltzaileak
      (let [eran (api-deia :post "erabiltzaileak" :json
                                    {:erabiltzailea "era1"
                                     :pasahitza "1234"
                                     :izena "Era"
                                     :deskribapena "Erabiltzaile bat naiz"})]
        (let [erab (:erabiltzailea eran)]
          (keys (:erabiltzailea eran)) => [:sortze_data :izena :deskribapena :erabiltzailea]
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

(fact "Erabiltzaile izen errepikatua" :erabiltzaileak
      (api-deia :post "erabiltzaileak" :ezer
                {:erabiltzailea "era"
                 :pasahitza "1234"
                 :izena "Era"})
      (let [egoera (api-deia :post "erabiltzaileak" :egoera
                             {:erabiltzailea "era"
                              :pasahitza "4321"
                              :izena "Era erre"})]
        egoera => 422))

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

(fact "Muga 0" :erabiltzaileak
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
      (let [eran (get-json "erabiltzaileak?muga=0")]
        (:guztira eran) => 3
        (:muga eran) => 0
        (let [era1 (first (:erabiltzaileak eran))]
          (:erabiltzailea era1) => "era1")
        (let [era2 (second (:erabiltzaileak eran))]
          (:erabiltzailea era2) => "era2")
        (let [era3 (nth (:erabiltzaileak eran) 2)]
          (:erabiltzailea era3) => "era3")))

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
    ; saioa amaitu
    (api-deia :delete (str "saioak/" (:token saioa)))))

(fact "Saioa hasi erabiltzailerik ez" :saioak
      (api-deia :post "saioak" :egoera
                {:erabiltzailea "era1"
                 :pasahitza "1234"}) => 422)

(fact "Saioa hasi pasahitz okerra" :saioak
      (api-deia :post "erabiltzaileak" :ezer
                {:erabiltzailea "era1"
                 :pasahitza "4321"
                 :izena "Era"})      
      (api-deia :post "saioak" :egoera
                {:erabiltzailea "era1"
                 :pasahitza "1234"}) => 422)

; ERABILTZAILEAK: token
; ---------------------
(fact "Erabiltzaile bat aldatu" :erabiltzaileak
      (let [token (saioa-hasi "era1" "1234" "Era")]
        (api-deia :put (str "erabiltzaileak/era1?token=" token) :ezer
                  {:pasahitza "1111"
                   :izena "Era berria"
                   :deskribapena "Aldatutako erabiltzaile bat naiz"}))      
      
      (let [eran (api-deia :get "erabiltzaileak/era1" :json)]
        (let [era1 (:erabiltzailea eran)]
          (:izena era1) => "Era berria"
          (:deskribapena era1) => "Aldatutako erabiltzaile bat naiz")))

(fact "Ez dagoen erabiltzaile bat aldatzen saiatu" :erabiltzaileak
      (let [egoera (api-deia :put "erabiltzaileak/era1?token=edozer" :egoera
                             {:pasahitza "1111"
                              :izena "Era berria"
                              :deskribapena "Aldatutako erabiltzaile bat naiz"})]
        egoera => 404))

(fact "Erabiltzaile bat aldatu token okerrarekin" :erabiltzaileak
      (api-deia :post "erabiltzaileak" :ezer
                {:erabiltzailea "era1"
                 :pasahitza "1234"
                 :izena "Era"
                 :deskribapena "Erabiltzaile bat naiz"})
      (let [egoera (api-deia :put "erabiltzaileak/era1?token=okerra" :egoera
                             {:pasahitza "1111"
                              :izena "Era berria"
                              :deskribapena "Aldatutako erabiltzaile bat naiz"})]
        egoera => 401))

(fact "Erabiltzaile bat aldatu beste token batekin" :erabiltzaileak
      (api-deia :post "erabiltzaileak" :ezer
                {:erabiltzailea "era1"
                 :pasahitza "1234"
                 :izena "Era"
                 :deskribapena "Erabiltzaile bat naiz"})
      (let [token (saioa-hasi "era2" "4321" "Era2")]
        (let [egoera (api-deia :put (str "erabiltzaileak/era1?token=" token) :egoera
                               {:pasahitza "1111"
                                :izena "Era berria"
                                :deskribapena "Aldatutako erabiltzaile bat naiz"})]
          egoera => 401)))

(fact "Ez dagoen erabiltzailea ezabatzen saiatu" :erabiltzaileak
      (let [egoera (api-deia :delete "erabiltzaileak/era1" :egoera)]
        egoera => 404))

(fact "Erabiltzaile bat ezabatu" :erabiltzaileak
      (let [token (saioa-hasi "era1" "1234" "Era")]
        (api-deia :delete (str "erabiltzaileak/era1?token=" token)))
      (let [egoera (api-deia :get "erabiltzaileak/era1" :egoera)]
        egoera => 404))

(fact "Erabiltzaile bat ezabatu token okerrarekin" :erabiltzaileak
      (api-deia :post "erabiltzaileak" :ezer
                {:erabiltzailea "era1"
                 :pasahitza "1234"
                 :izena "Era"
                 :deskribapena "Erabiltzaile bat naiz"})
      (let [egoera (api-deia :delete "erabiltzaileak/era1?token=ezdago" :egoera)]
        egoera => 401))

(fact "Erabiltzaile bat ezabatu beste token batekin" :erabiltzaileak
      (api-deia :post "erabiltzaileak" :ezer
                {:erabiltzailea "era1"
                 :pasahitza "1234"
                 :izena "Era"
                 :deskribapena "Erabiltzaile bat naiz"})
      (let [token (saioa-hasi "era2" "4321" "Era2")]
        (let [egoera (api-deia :delete (str "erabiltzaileak/era1?token=" token) :egoera)]
          egoera => 401)))

; LIBURUAK
; --------
(fact "Liburua gehitu" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"
                 :sinopsia "Duela urte asko..."
                 :argitaletxea "Etxea"
                 :urtea "2009"
                 :generoa "Eleberria"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}
          eran (api-deia :post (str "liburuak?token=" token) :json param)]
      (let [lib (:liburua eran)]
        (:id lib) => 1
        (:erabiltzailea lib) => "era"
        ; Aurrez ezin dugu jakin zer magnet sortuko den
        (:titulua lib) => (:titulua param)
        (:egileak lib) => (:egileak param)
        (:hizkuntza lib) => (:hizkuntza param)
        (:sinopsia lib) => (:sinopsia param)
        (:argitaletxea lib) => (:argitaletxea param)
        (:urtea lib) => (:urtea param)
        (:generoa lib) => (:generoa param)
        (:etiketak lib) => (:etiketak param)
        ; Azalaren izena zein izango den ez dakigu
        ; 0 iruzkinekin hasiko da
        (:iruzkin_kopurua lib) => 0
        ; 0 gogokoekin hasiko da
        (:gogoko_kopurua lib) => 0))))

(fact "Eremu bat falta duen liburua gehitu" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"
                 :sinopsia "Duela urte asko..."
                 :argitaletxea "Etxea"
                 :urtea "2009"
                 :generoa "Eleberria"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}
          eremu-gabe #(api-deia :post (str "liburuak?token=" token) :egoera (dissoc param %))]
        (eremu-gabe :epub) => 422
        (eremu-gabe :titulua) => 422
        (eremu-gabe :egileak) => 422
        (eremu-gabe :hizkuntza) => 422        
        (eremu-gabe :sinopsia) => 422
        (eremu-gabe :argitaletxea) => 200
        (eremu-gabe :urtea) => 422
        (eremu-gabe :generoa) => 200
        (eremu-gabe :etiketak) => 422
        (eremu-gabe :azala) => 422)))

(fact "Token okerrarekin liburua gehitu" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"
                 :sinopsia "Duela urte asko..."
                 :urtea "2009"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}]
      (api-deia :post "liburuak?token=okerra" :egoera param) => 401)))

(fact "Liburua lortu" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"
                 :sinopsia "Duela urte asko..."
                 :urtea "2009"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}]
      (let [{{id :id} :liburua} (api-deia :post (str "liburuak?token=" token) :json param)
            {lib :liburua} (api-deia :get (str "liburuak/" id) :json)]
        (:titulua lib) => (:titulua param)
        (:egileak lib) => (:egileak param)
        (:hizkuntza lib) => (:hizkuntza param)        
        (:sinopsia lib) => (:sinopsia param)
        (:urtea lib) => (:urtea param)
        (:etiketak lib) => (:etiketak param)))))

(fact "Existitzen ez den liburua" :liburuak
  (api-deia :get "liburuak/1" :egoera) => 404)

(fact "Liburua aldatu" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"
                 :sinopsia "Duela urte asko..."
                 :urtea "2009"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}]
      (let [{{id :id} :liburua} (api-deia :post (str "liburuak?token=" token) :json param)
            {lib :liburua} (api-deia :put (str "liburuak/" id "?token=" token) :json (assoc param :titulua "Titulu berria"))]
        (:titulua lib) => "Titulu berria"
        (:egileak lib) => (:egileak param)
        (:hizkuntza lib) => (:hizkuntza param)        
        (:sinopsia lib) => (:sinopsia param)
        (:urtea lib) => (:urtea param)
        (:etiketak lib) => (:etiketak param)))))

(fact "Liburua aldatu eremu okerrak" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"
                 :sinopsia "Duela urte asko..."
                 :urtea "2009"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}]
      (let [{{id :id} :liburua} (api-deia :post (str "liburuak?token=" token) :json param)]
        (api-deia :put (str "liburuak/" id "?token=okerra") :egoera (dissoc param :titulua)) => 422))))

(fact "Liburua aldatu token okerra" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"
                 :sinopsia "Duela urte asko..."
                 :urtea "2009"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}]
      (let [{{id :id} :liburua} (api-deia :post (str "liburuak?token=" token) :json param)]
        (let [token (saioa-hasi "era2" "4321" "Era2")]
          (api-deia :put (str "liburuak/" id "?token=" token) :egoera param) => 401)
        (api-deia :put (str "liburuak/" id "?token=okerra") :egoera param) => 401))))

(fact "Existitzen ez den liburu bat aldatu" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")
        param {:epub "base64"
               :titulua "Kaixo mundua"
               :egileak ["Joxe" "Patxi"]
               :hizkuntza "euskara"               
               :sinopsia "Duela urte asko..."
               :urtea "2009"
               :etiketak ["kaixo" "joxe" "zaharra"]
               :azala "base64"}]
    (api-deia :put (str "liburuak/" 999 "?token=" token) :egoera param) => 404))

(fact "Liburu bat ezabatu" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"                 
                 :sinopsia "Duela urte asko..."
                 :urtea "2009"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}]
      (let [{{id :id} :liburua} (api-deia :post (str "liburuak?token=" token) :json param)]
        (api-deia :delete (str "liburuak/" id "?token=" token) :egoera) => 200
        (api-deia :get (str "liburuak/" id) :egoera param) => 404))))

(fact "Liburu bat ezabatu token okerrarekin" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"                 
                 :sinopsia "Duela urte asko..."
                 :urtea "2009"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}]
      (let [{{id :id} :liburua} (api-deia :post (str "liburuak?token=" token) :json param)]
        (let [token (saioa-hasi "era2" "4321" "Era2")]
          (api-deia :delete (str "liburuak/" id "?token=" token) :egoera) => 401)
        (api-deia :delete (str "liburuak/" id "?token=okerra") :egoera) => 401))))

(fact "Existitzen ez den liburu bat ezabatu" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"                 
                 :sinopsia "Duela urte asko..."
                 :urtea "2009"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}]
      (let [{{id :id} :liburua} (api-deia :post (str "liburuak?token=" token) :json param)]
        (api-deia :delete (str "liburuak/666?token=" token) :egoera) => 404))))

(fact "Liburuak lortu" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"                 
                 :sinopsia "Duela urte asko..."
                 :urtea "2009"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}]
      (api-deia :post (str "liburuak?token=" token) :ezer param)
      (api-deia :post (str "liburuak?token=" token) :ezer (assoc param :titulua "Beste liburu bat"))
      (api-deia :post (str "liburuak?token=" token) :ezer (assoc param :titulua "Hirugarrena"))
      (let [eran (api-deia :get "liburuak" :json)]
        (:guztira eran) => 3
        (let [liburuak (:liburuak eran)]
          (count liburuak) => 3
          (:titulua (first liburuak)) => "Kaixo mundua"
          (:titulua (second liburuak)) => "Beste liburu bat"
          (:titulua (liburuak 2)) => "Hirugarrena")))))

(fact "Erabiltzaile baten liburuak lortu" :liburuak
  (let [token (saioa-hasi "era" "1234" "Era")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"                 
                 :sinopsia "Duela urte asko..."
                 :urtea "2009"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}]
      (api-deia :post (str "liburuak?token=" token) :ezer param)
      (api-deia :post (str "liburuak?token=" token) :ezer (assoc param :titulua "Beste liburu bat"))
      (api-deia :post (str "liburuak?token=" token) :ezer (assoc param :titulua "Hirugarrena"))
      (let [eran (api-deia :get "erabiltzaileak/era/liburuak" :json)]
        (println eran)
        (:guztira eran) => 3
        (let [liburuak (:liburuak eran)]
          (count liburuak) => 3
          (:titulua (first liburuak)) => "Kaixo mundua"
          (:titulua (second liburuak)) => "Beste liburu bat"
          (:titulua (liburuak 2)) => "Hirugarrena")))))

(defn gehitu-liburua
  "Liburu bat gehitzen du eta saioaren tokena eta liburuaren id itzultzen ditu"
  [era pas]
  (let [token (saioa-hasi era pas "izena")]
    (let [param {:epub "base64"
                 :titulua "Kaixo mundua"
                 :egileak ["Joxe" "Patxi"]
                 :hizkuntza "euskara"                 
                 :sinopsia "Duela urte asko..."
                 :urtea "2009"
                 :etiketak ["kaixo" "joxe" "zaharra"]
                 :azala "base64"}]
      (let [{{id :id} :liburua} (api-deia :post (str "liburuak?token=" token) :json param)]
        [token id]))))

(fact "Iruzkina gehitu" :iruzkinak
      (let [[token id] (gehitu-liburua "era" "1234")]
        (let [{ir :iruzkina} (api-deia :post (str "liburuak/" id  "/iruzkinak?token=" token) :json
                                       {:gurasoak []
                                        :edukia "Hau iruzkin bat da"})]
          (:liburua ir) => (str id)
          (:erabiltzailea ir) => "era"
          (:gurasoak ir) => []
          (:erantzunak ir) => []
          (:edukia ir) => "Hau iruzkin bat da")
        (let [{lib :liburua} (api-deia :get (str "liburuak/" id) :json)]
          (:iruzkin_kopurua lib) => 1)))

(fact "Iruzkina gehitu token okerrarekin" :iruzkinak
      (let [[token id] (gehitu-liburua "era" "1234")]
        (api-deia :post (str "liburuak/" id  "/iruzkinak?token=okerra") :egoera
                  {:gurasoak []
                   :edukia "Hau iruzkin bat da"}) => 401))

(fact "Iruzkina aldatu" :iruzkinak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (let [{{id :id} :iruzkina} (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :json
                                             {:gurasoak []
                                              :edukia "Hau iruzkin bat da"})]
          (let [{ir :iruzkina} (api-deia :put (str "iruzkinak/" id  "?token=" token) :json
                                         {:gurasoak []
                                          :edukia "Eduki berria"})]
            (:edukia ir) => "Eduki berria"))))

(fact "Iruzkina aldatu token okerrarekin" :iruzkinak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (let [{{id :id} :iruzkina} (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :json
                                             {:gurasoak []
                                              :edukia "Hau iruzkin bat da"})]
          (let [token (saioa-hasi "era2" "4321" "Era2")]
                      (api-deia :put (str "iruzkinak/" id  "?token=" token) :egoera
                                {:gurasoak []
                                 :edukia "Eduki berria"}) => 401)
          (api-deia :put (str "iruzkinak/" id  "?token=okerra") :egoera
                    {:gurasoak []
                     :edukia "Eduki berria"}) => 401)))

(fact "Existitzen ez den iruzkina aldatu" :iruzkinak
      (api-deia :put (str "iruzkinak/999?token=edozer") :egoera
                {:gurasoak []
                 :edukia "Eduki berria"}) => 404)

(fact "Iruzkina lortu" :iruzkinak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (let [{{id :id} :iruzkina} (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :json
                                             {:gurasoak []
                                              :edukia "Hau iruzkin bat da"})]
          (let [{ir :iruzkina} (api-deia :get (str "iruzkinak/" id) :json)]
            (:gurasoak ir) => []
            (:erantzunak ir) => []
            (:edukia ir) => "Hau iruzkin bat da"))))

(fact "Existitzen ez den iruzkina lortu" :iruzkinak
      (api-deia :get "iruzkinak/999" :egoera) => 404)

(fact "Iruzkina ezabatu" :iruzkinak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (let [{{id :id} :iruzkina} (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :json
                                             {:gurasoak []
                                              :edukia "Hau iruzkin bat da"})]
          (api-deia :delete (str "iruzkinak/" id "?token=" token))
          (api-deia :get (str "iruzkinak/" id) :egoera) => 404)))

(fact "Iruzkina ezabatu token okerrarekin" :iruzkinak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (let [{{id :id} :iruzkina} (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :json
                                             {:gurasoak []
                                              :edukia "Hau iruzkin bat da"})]
          (api-deia :delete (str "iruzkinak/" id  "?token=okerra") :egoera) => 401))      
      (let [token (saioa-hasi "era" "1234" "Era")]))

(fact "Iruzkina ezabatu beste token batekin" :iruzkinak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (let [{{id :id} :iruzkina} (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :json
                                             {:gurasoak []
                                              :edukia "Hau iruzkin bat da"})]
          (let [token (saioa-hasi "era2" "4321" "Era2")]
            (api-deia :delete (str "iruzkinak/" id  "?token=" token) :egoera)) => 401)))

(fact "Existitzen ez den iruzkina ezabatu" :iruzkinak
      (api-deia :delete (str "iruzkinak/999?token=edozer") :egoera) => 404)

(fact "Iruzkinak lortu" :iruzkinak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (let [{{id1 :id} :iruzkina}
              (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :json
                        {:gurasoak []
                         :edukia "Hau iruzkin bat da"})]
          (let [{{id2 :id} :iruzkina}
                (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :json
                          {:gurasoak [id1]
                           :edukia "Hau beste iruzkin bat da"})]
            (let [{{id3 :id} :iruzkina}
                  (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :json
                            {:gurasoak [id1 id2]
                             :edukia "Hirugarrena"})]
              (let [eran (api-deia :get "iruzkinak" :json)]
                (:guztira eran) => 3
                (let [irak (:iruzkinak eran)]
                  (count irak) => 3
                  (:gurasoak (first irak)) => []
                  (:erantzunak (first irak)) => [id2 id3]
                  (:edukia (first irak)) => "Hau iruzkin bat da"
                  (:gurasoak (second irak)) => [id1]
                  (:erantzunak (second irak)) => [id3]                
                  (:edukia (second irak)) => "Hau beste iruzkin bat da"
                  (:gurasoak (nth irak 2)) => [id1 id2]
                  (:erantzunak (nth irak 2)) => []                                
                  (:edukia (nth irak 2)) => "Hirugarrena")))))))

(fact "Liburuaren iruzkinak" :iruzkinak
      (let [[token libid] (gehitu-liburua "era2" "4321")]
        (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :ezer
                  {:gurasoak []
                   :edukia "Hau beste liburu bat"}))
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :ezer
                  {:gurasoak []
                   :edukia "Hau iruzkin bat da"})
        (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :ezer
                  {:gurasoak []
                   :edukia "Hau beste iruzkin bat da"})
        (let [eran (api-deia :get (str "liburuak/" libid "/iruzkinak") :json)]
          (:guztira eran) => 2
          (let [irak (:iruzkinak eran)]
            (count irak) => 2
            (:edukia (first irak)) => "Hau iruzkin bat da"
            (:edukia (second irak)) => "Hau beste iruzkin bat da"))))

(fact "Erabiltzaile baten iruzkinak" :iruzkinak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :ezer
                  {:gurasoak []
                   :edukia "Hau iruzkin bat da"})
        (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :ezer
                  {:gurasoak []
                   :edukia "Hau beste iruzkin bat da"})
        (let [token (saioa-hasi "era2" "4321")]
          (api-deia :post (str "liburuak/" libid  "/iruzkinak?token=" token) :ezer
                    {:gurasoak []
                     :edukia "Hau beste erabiltzaile batena"}))
        (let [eran (api-deia :get "erabiltzaileak/era/iruzkinak" :json)]
          (:guztira eran) => 2
          (let [irak (:iruzkinak eran)]
            (count irak) => 2
            (:edukia (first irak)) => "Hau iruzkin bat da"
            (:edukia (second irak)) => "Hau beste iruzkin bat da"))))

(fact "Gogokoa gehitu" :gogokoak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (let [{lib :gogoko_liburua} (api-deia :post (str "erabiltzaileak/era/gogoko_liburuak?token=" token) :json
                                              {:id libid})]
          (:id lib) => libid
          (:erabiltzailea lib) => "era")))

(fact "Existitzen ez den gogokoa gehitu" :gogokoak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (api-deia :post (str "erabiltzaileak/era/gogoko_liburuak?token=" token) :egoera
                  {:id 999}) => 422))

(fact "Gogokoa gehitu token okerrarekin" :gogokoak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (api-deia :post "erabiltzaileak/era/gogoko_liburuak?token=okerra" :egoera
                  {:id libid}) => 401))

(fact "Gogokoak lortu" :gogokoak
      (let [[token lib1id] (gehitu-liburua "era" "1234")
            _ (api-deia :post (str "erabiltzaileak/era/gogoko_liburuak?token=" token) :ezer
                        {:id lib1id})
            [token lib2id] (gehitu-liburua "era" "1234")
            _ (api-deia :post (str "erabiltzaileak/era/gogoko_liburuak?token=" token) :ezer
                        {:id lib2id})
            eran (api-deia :get "erabiltzaileak/era/gogoko_liburuak" :json)
            libuk (:gogoko_liburuak eran)]
        (:guztira eran) => 2
        (count libuk) => 2
        (:id (first libuk)) => lib1id
        (:id (second libuk)) => lib2id))

(fact "Gogokoak ezabatu" :gogokoak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (api-deia :post (str "erabiltzaileak/era/gogoko_liburuak?token=" token) :ezer
                  {:id libid})
        (:guztira (api-deia :get "erabiltzaileak/era/gogoko_liburuak" :json)) => 1
        (api-deia :delete (str "erabiltzaileak/era/gogoko_liburuak/" libid "?token=" token))
        (:guztira (api-deia :get "erabiltzaileak/era/gogoko_liburuak" :json)) => 0))

(fact "Gogokoak ezabatu token okerra" :gogokoak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (api-deia :post (str "erabiltzaileak/era/gogoko_liburuak?token=" token) :ezer
                  {:id libid})
        (:guztira (api-deia :get "erabiltzaileak/era/gogoko_liburuak" :json)) => 1
        (api-deia :delete (str "erabiltzaileak/era/gogoko_liburuak/" libid "?token=okerra") :egoera) => 401))

(fact "Gogokoak ezabatu existitu ez" :gogokoak
      (let [[token libid] (gehitu-liburua "era" "1234")]
        (api-deia :delete (str "erabiltzaileak/era/gogoko_liburuak/" 999 "?token=" token) :egoera) => 422))

(fact "Gogoko erabiltzaileak" :gogokoak
      (let [[token lib1id] (gehitu-liburua "era" "1234")
            _ (api-deia :post (str "erabiltzaileak/era/gogoko_liburuak?token=" token) :ezer
                        {:id lib1id})
            [token lib2id] (gehitu-liburua "era" "1234")
            _ (api-deia :post (str "erabiltzaileak/era/gogoko_liburuak?token=" token) :ezer
                        {:id lib2id})
            eran (api-deia :get (str "liburuak/" lib1id "/gogoko_erabiltzaileak") :json)
            erak (:gogoko_erabiltzaileak eran)]
        (:guztira eran) => 1
        (count erak) => 1
        (:erabiltzailea (first erak)) => "era"))

(fact "Argitaletxe guztiak" :hainbat
      (let [token (saioa-hasi "era" "1234")
            param {:epub "base64"
                   :titulua "Kaixo mundua"
                   :egileak ["Joxe" "Patxi"]
                   :hizkuntza "euskara"                 
                   :sinopsia "Duela urte asko..."
                   :argitaletxea "etxe1"
                   :urtea "2009"
                   :etiketak ["kaixo" "joxe" "zaharra"]
                   :azala "base64"}
            _ (api-deia :post (str "liburuak?token=" token) :json param)
            _ (api-deia :post (str "liburuak?token=" token) :json (assoc param
                                                                    :titulua "tit2"
                                                                    :argitaletxea "etxe2"))
            eran (api-deia :get "argitaletxeak" :json)
            argit (:argitaletxeak eran)]
        (:guztira eran) => 2
        (count argit) => 2
        (first argit) => "etxe1"
        (second argit) => "etxe2"))
