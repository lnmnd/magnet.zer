(ns ^{:doc "Bideak, APIaren sarrera puntuak."}
  magnet.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.cors :refer [wrap-cors]]
            [clj-json.core :as json]
            [magnet.erabiltzaileak :as erak]
            [magnet.saioak :as saioak]
            [magnet.liburuak :as liburuak]
            [magnet.iruzkinak :as iruzkinak]
            [magnet.hainbat :as hainbat]))

(defn- json-erantzuna
  "Datuak JSON formatuan itzultzen ditu. Egoera aukeran, 200 lehenetsia."
  [datuak & [egoera]]
  {:status (or egoera 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string datuak)})

(defn- egoera-zenbakia
  "Egoerari dagokion zenbakia itzultzen du."
  [egoera]
  (egoera {:ok 200
           :baimenik-ez 401
           :ez-dago 404
           :ezin-prozesatu 422}))

(defn- egoera-gorputza
  "Egoerari dagokion gorputza itzultzen du."
  [egoera]
  (egoera {:baimenik-ez
           {:mezua "Baimenik ez"
            :deskribapena "Baimenik ez, kautotzea beharrezkoa"}
           :ez-dago
           {:mezua "Ez dago"
            :deskribapena "Baliabidea ez da aurkitu"}
           :ezin-prozesatu
           {:mezua "Ezin prozesatu"
            :deskribapena "Eskaerak sintaxi egokia du baina errore semantikoak ditu"}}))

(defmacro ^:private api-erantzuna
  "Metodo, url, parametro eta edukia emanik erantzuna osatzen du"
  [metodoa url params edukia]
  `(~metodoa ~(str "/v1/" url) ~params
       (let [[egoera# datuak#] ~edukia]
         (json-erantzuna (if datuak# datuak# (egoera-gorputza egoera#)) (egoera-zenbakia egoera#)))))

(defmacro ^:private hainbat-erantzuna
  "Hainbat motako API erantzuna osatzen du."
  [muga db-kon hel fun]
  `(api-erantzuna GET ~hel eskaera#
                  (let [qp# (:query-params eskaera#)]
                    (orriztatu ~muga ~fun qp# ~db-kon))))

(defmacro orriztatu
  "Funtzioari desplazamendua eta muga parametroak gehitzen dizkio."
  [mug fn qparam & param]
  `(let [muga# (if (contains? ~qparam "muga")
                 (read-string (~qparam "muga"))
                 ~mug)
         desp# (if (contains? ~qparam "desplazamendua")
                 (read-string (~qparam "desplazamendua"))
                 0)]
     (~fn desp# (if (> muga# 0) muga# 0) ~@param)))

(defn bideak-sortu
  "Konfigurazioa eta saioak emanda bideak sortzen ditu."
  [konfig saioak-osagaia]
  (routes
    (api-erantzuna GET "erabiltzaileak" eskaera
                   (let [{query-params :query-params} eskaera]
                     (orriztatu (:muga konfig) erak/lortu-bilduma query-params (:db-kon konfig))))
    (api-erantzuna GET "erabiltzaileak/:erabiltzailea" {{erabiltzailea :erabiltzailea} :params}
                   (erak/lortu (:db-kon konfig) erabiltzailea))
    (api-erantzuna POST "erabiltzaileak" eskaera
                   (let [edukia (json/parse-string (slurp (:body eskaera)) true)]
                     (erak/gehitu! (:db-kon konfig) edukia)))
    (api-erantzuna PUT "erabiltzaileak/:erabiltzailea" eskaera
                   (let [token (:token (:params eskaera))
                         erabiltzailea (:erabiltzailea (:params eskaera))
                         edukia (json/parse-string (slurp (:body eskaera)) true)]
                     (erak/aldatu! saioak-osagaia (:db-kon konfig) token erabiltzailea edukia)))
    (api-erantzuna DELETE "erabiltzaileak/:erabiltzailea" eskaera
                   (let [token (:token (:params eskaera))
                         erabiltzailea (:erabiltzailea (:params eskaera))]
                     (erak/ezabatu! saioak-osagaia (:db-kon konfig) token erabiltzailea)))

                                        ; saioak
    (api-erantzuna POST "saioak" eskaera
                   (let [edukia (json/parse-string (slurp (:body eskaera)) true)]
                     (saioak/hasi! saioak-osagaia (:db-kon konfig) (:saio-iraungitze-denbora konfig) (:erabiltzailea edukia) (:pasahitza edukia))))
    (api-erantzuna DELETE "saioak/:token" {{token :token} :params}
                   (saioak/amaitu! saioak-osagaia token))

                                        ; liburuak
    (api-erantzuna GET "liburuak" eskaera
                   (let [{query-params :query-params} eskaera]
                     (orriztatu (:muga konfig) liburuak/lortu-bilduma query-params (:db-kon konfig))))
    (api-erantzuna GET "erabiltzaileak/:erabiltzailea/liburuak" eskaera
                   (let [{era :erabiltzailea} (:params eskaera)
                         {query-params :query-params} eskaera]
                     (orriztatu (:muga konfig) liburuak/lortu-erabiltzailearenak query-params (:db-kon konfig) era)))  
    (api-erantzuna GET "liburuak/:id" {{id :id} :params}
                   (liburuak/lortu (:db-kon konfig) id))
    (api-erantzuna POST "liburuak" eskaera
                   (let [token (:token (:params eskaera))
                         edukia (json/parse-string (slurp (:body eskaera)) true)]
                     (liburuak/gehitu! saioak-osagaia (:db-kon konfig) (:partekatu konfig) (:kokapenak konfig) (:torrent-gehitze-programa konfig) (:trackerrak konfig) token edukia)))
    (api-erantzuna PUT "liburuak/:id" eskaera
                   (let [token (:token (:params eskaera))
                         id (:id (:params eskaera))
                         edukia (json/parse-string (slurp (:body eskaera)) true)]
                     (liburuak/aldatu! saioak-osagaia (:db-kon konfig) token id edukia)))
    (api-erantzuna DELETE "liburuak/:id" eskaera
                   (let [token (:token (:params eskaera))
                         id (:id (:params eskaera))]
                     (liburuak/ezabatu! saioak-osagaia (:db-kon konfig) token id)))  

                                        ; iruzkinak
    (api-erantzuna POST "liburuak/:id/iruzkinak" eskaera
                   (let [token (:token (:params eskaera))
                         id (:id (:params eskaera))
                         edukia (json/parse-string (slurp (:body eskaera)) true)]
                     (iruzkinak/gehitu! saioak-osagaia (:db-kon konfig) token id edukia)))
    (api-erantzuna PUT "iruzkinak/:id" eskaera
                   (let [token (:token (:params eskaera))
                         id (:id (:params eskaera))
                         edukia (json/parse-string (slurp (:body eskaera)) true)]
                     (iruzkinak/aldatu! saioak-osagaia (:db-kon konfig) token id edukia)))
    (api-erantzuna GET "iruzkinak/:id" eskaera
                   (let [id (:id (:params eskaera))]
                     (iruzkinak/lortu (:db-kon konfig) id)))
    (api-erantzuna DELETE "iruzkinak/:id" eskaera
                   (let [token (:token (:params eskaera))
                         id (:id (:params eskaera))]
                     (iruzkinak/ezabatu! saioak-osagaia (:db-kon konfig) token id)))
    (api-erantzuna GET "iruzkinak" eskaera
                   (let [{query-params :query-params} eskaera]
                     (orriztatu (:muga konfig) iruzkinak/lortu-bilduma query-params (:db-kon konfig))))
    (api-erantzuna GET "liburuak/:id/iruzkinak" eskaera
                   (let [id (:id (:params eskaera))
                         {query-params :query-params} eskaera]
                     (orriztatu (:muga konfig) iruzkinak/lortu-liburuarenak query-params (:db-kon konfig) id)))
    (api-erantzuna GET "erabiltzaileak/:erabiltzailea/iruzkinak" eskaera
                   (let [erabiltzailea (:erabiltzailea (:params eskaera))
                         {query-params :query-params} eskaera]
                     (orriztatu (:muga konfig) iruzkinak/lortu-erabiltzailearenak query-params (:db-kon konfig) erabiltzailea)))    

                                        ; gogokoak
    (api-erantzuna POST "erabiltzaileak/:erabiltzailea/gogoko_liburuak" eskaera
                   (let [token (:token (:params eskaera))
                         {id :id} (json/parse-string (slurp (:body eskaera)) true)]
                     (liburuak/gehitu-gogokoa! saioak-osagaia (:db-kon konfig) token id)))
    (api-erantzuna DELETE "erabiltzaileak/:erabiltzailea/gogoko_liburuak/:id" eskaera
                   (let [token (:token (:params eskaera))
                         id (:id (:params eskaera))]
                     (liburuak/ezabatu-gogokoa! saioak-osagaia (:db-kon konfig) token id)))    
    (api-erantzuna GET "erabiltzaileak/:erabiltzailea/gogoko_liburuak" eskaera
                   (let [erabiltzailea (:erabiltzailea (:params eskaera))
                         {query-params :query-params} eskaera]
                     (orriztatu (:muga konfig) liburuak/lortu-gogokoak query-params (:db-kon konfig) erabiltzailea)))
    (api-erantzuna GET "liburuak/:id/gogoko_erabiltzaileak" eskaera
                   (let [id (:id (:params eskaera))
                         {query-params :query-params} eskaera]
                     (orriztatu (:muga konfig) erak/gogoko-erabiltzaileak query-params (:db-kon konfig) id)))

                                        ; hainbat
    (hainbat-erantzuna (:muga konfig) (:db-kon konfig) "tituluak" hainbat/tituluak)
    (hainbat-erantzuna (:muga konfig) (:db-kon konfig) "egileak" hainbat/egileak)
    (hainbat-erantzuna (:muga konfig) (:db-kon konfig) "argitaletxeak"  hainbat/argitaletxeak)
    (hainbat-erantzuna (:muga konfig) (:db-kon konfig) "generoak"  hainbat/generoak)
    (hainbat-erantzuna (:muga konfig) (:db-kon konfig) "etiketak"  hainbat/etiketak)
    (hainbat-erantzuna (:muga konfig) (:db-kon konfig) "urteak" hainbat/urteak)
    (hainbat-erantzuna (:muga konfig) (:db-kon konfig) "hizkuntzak" hainbat/hizkuntzak)
    
    (route/files "/" {:root (:publikoa (:kokapenak konfig))})
    (route/not-found "Not Found")))

(defn handler-sortu
  "Konfigurazioa eta saioak emanda handler sortzen du.
   CORS gaitzen du."
  [konfig saioak-osagaia]
  (-> (bideak-sortu konfig saioak-osagaia)
      handler/site
      (wrap-cors
       :access-control-allow-headers "Access-Control-Allow-Origin, X-Requested-With, Content-Type, Accept"
       :access-control-allow-origin #".*"
       :access-control-allow-methods [:get :put :post :delete])))
