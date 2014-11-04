(ns magnet.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.cors :refer [wrap-cors]]
            [clj-json.core :as json]
            [magnet.konfig :as konfig]
            [magnet.erabiltzaileak :as erak]
            [magnet.saioak :as saioak]
            [magnet.liburuak :as liburuak]
            [magnet.iruzkinak :as iruzkinak]))

(defn- json-erantzuna
  "Datuak JSON formatuan itzultzen ditu. Egoera aukeran, 200 lehenetsia."
  [datuak & [egoera]]
  {:status (or egoera 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string datuak)})

(defn- egoera-zenbakia [egoera]
  (egoera {:ok 200
           :baimenik-ez 401
           :ez-dago 404
           :ezin-prozesatu 422}))

(defmacro ^:private api-erantzuna
  "Metodo, url, parametro eta edukia emanik erantzuna osatzen du"
  [metodoa url params edukia]
  `(~metodoa ~(str "/v1/" url) ~params
       (let [[egoera# datuak#] ~edukia]
         (json-erantzuna (if datuak# datuak# {}) (egoera-zenbakia egoera#)))))

(defmacro orriztatu
  "Funtzioari desplazamendua eta muga parametroak gehitzen dizkio."
  [fn qparam & param]
  `(let [muga# (if (contains? ~qparam "muga")
                 (read-string (~qparam "muga"))
                 konfig/muga)
         desp# (if (contains? ~qparam "desplazamendua")
                 (read-string (~qparam "desplazamendua"))
                 0)]
     (~fn desp# (if (> muga# 0) muga# 0) ~@param)))

(defroutes app-routes
  (api-erantzuna GET "erabiltzaileak" eskaera
                 (let [{query-params :query-params} eskaera]
                   (orriztatu erak/lortu-bilduma query-params)))
  (api-erantzuna GET "erabiltzaileak/:erabiltzailea" {{erabiltzailea :erabiltzailea} :params}
                 (erak/lortu erabiltzailea))
  (api-erantzuna POST "erabiltzaileak" eskaera
                 (let [edukia (json/parse-string (slurp (:body eskaera)) true)]
                   (erak/gehitu! edukia)))
  (api-erantzuna PUT "erabiltzaileak/:erabiltzailea" eskaera
                 (let [token (:token (:params eskaera))
                       erabiltzailea (:erabiltzailea (:params eskaera))
                       edukia (json/parse-string (slurp (:body eskaera)) true)]
                   (erak/aldatu! token erabiltzailea edukia)))
  (api-erantzuna DELETE "erabiltzaileak/:erabiltzailea" eskaera
                 (let [token (:token (:params eskaera))
                       erabiltzailea (:erabiltzailea (:params eskaera))]
                   (erak/ezabatu! token erabiltzailea)))

  ; saioak
  (api-erantzuna POST "saioak" eskaera
                 (let [edukia (json/parse-string (slurp (:body eskaera)) true)]
                   (saioak/hasi! (:erabiltzailea edukia) (:pasahitza edukia))))
  (api-erantzuna DELETE "saioak/:token" {{token :token} :params}
                 (saioak/amaitu! token))

  ; liburuak
  (api-erantzuna GET "liburuak" eskaera
                 (let [{query-params :query-params} eskaera]
                   (orriztatu liburuak/lortu-bilduma query-params)))
  (api-erantzuna GET "erabiltzaileak/:erabiltzailea/liburuak" eskaera
                 (let [{era :erabiltzailea} (:params eskaera)
                       {query-params :query-params} eskaera]
                   (orriztatu liburuak/lortu-erabiltzailearenak query-params era)))  
  (api-erantzuna GET "liburuak/:id" {{id :id} :params}
                 (liburuak/lortu id))
  (api-erantzuna POST "liburuak" eskaera
                 (let [token (:token (:params eskaera))
                       edukia (json/parse-string (slurp (:body eskaera)) true)]
                   (liburuak/gehitu! token edukia)))
  (api-erantzuna PUT "liburuak/:id" eskaera
                 (let [token (:token (:params eskaera))
                       id (:id (:params eskaera))
                       edukia (json/parse-string (slurp (:body eskaera)) true)]
                   (liburuak/aldatu! token id edukia)))
  (api-erantzuna DELETE "liburuak/:id" eskaera
                 (let [token (:token (:params eskaera))
                       id (:id (:params eskaera))]
                   (liburuak/ezabatu! token id)))  

  ; iruzkinak
  (api-erantzuna POST "liburuak/:id/iruzkinak" eskaera
                 (let [token (:token (:params eskaera))
                       id (:id (:params eskaera))
                       edukia (json/parse-string (slurp (:body eskaera)) true)]
                   (iruzkinak/gehitu! token id edukia)))
    (api-erantzuna PUT "iruzkinak/:id" eskaera
                 (let [token (:token (:params eskaera))
                       id (:id (:params eskaera))
                       edukia (json/parse-string (slurp (:body eskaera)) true)]
                   (iruzkinak/aldatu! token id edukia)))
    (api-erantzuna GET "iruzkinak/:id" eskaera
                   (let [id (:id (:params eskaera))]
                     (iruzkinak/lortu id)))
    (api-erantzuna DELETE "iruzkinak/:id" eskaera
                   (let [token (:token (:params eskaera))
                         id (:id (:params eskaera))]
                     (iruzkinak/ezabatu! token id)))
    (api-erantzuna GET "iruzkinak" eskaera
                   (let [{query-params :query-params} eskaera]
                   (orriztatu iruzkinak/lortu-bilduma query-params)))
    (api-erantzuna GET "liburuak/:id/iruzkinak" eskaera
                   (let [id (:id (:params eskaera))
                         {query-params :query-params} eskaera]
                     (orriztatu iruzkinak/lortu-liburuarenak query-params id)))
    (api-erantzuna GET "erabiltzaileak/:erabiltzailea/iruzkinak" eskaera
                   (let [erabiltzailea (:erabiltzailea (:params eskaera))
                         {query-params :query-params} eskaera]
                     (orriztatu iruzkinak/lortu-erabiltzailearenak query-params erabiltzailea)))    
  
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (wrap-cors
       :access-control-allow-headers "Access-Control-Allow-Origin, X-Requested-With, Content-Type, Accept"
       :access-control-allow-origin #".*"
       :access-control-allow-methods [:get :put :post :delete])))
