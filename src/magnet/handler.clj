(ns magnet.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.cors :refer [wrap-cors]]
            [clj-json.core :as json]
            [magnet.konfig :as konfig]
            [magnet.erabiltzaileak :as erak]))

(defn json-erantzuna
  "Datuak JSON formatuan itzultzen ditu. Egoera aukeran, 200 lehenetsia."
  [datuak & [egoera]]
  {:status (or egoera 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string datuak)})

(defroutes app-routes
  (GET "/v1/erabiltzaileak" []
       (let [[datuak egoera] (erak/lortu-bilduma)]
         (json-erantzuna datuak egoera)))
  (GET "/v1/erabiltzaileak/:erabiltzailea" {{erabiltzailea :erabiltzailea} :params}
       (let [[datuak egoera] (erak/lortu erabiltzailea)]
         (json-erantzuna datuak egoera)))
  (POST "/v1/erabiltzaileak" eskaera
        (let [edukia (json/parse-string (slurp (:body eskaera)) true)
              [datuak egoera] (erak/sortu! edukia)]
          (json-erantzuna datuak egoera)))
  (PUT "/v1/erabiltzaileak/:erabiltzailea" eskaera
        (let [erabiltzailea (:erabiltzailea (:params eskaera))
              edukia (json/parse-string (slurp (:body eskaera)) true)
              [datuak egoera] (erak/aldatu! erabiltzailea edukia)]
          (json-erantzuna datuak egoera)))
  (DELETE "/v1/erabiltzaileak/:erabiltzailea" {{erabiltzailea :erabiltzailea} :params}
        (let [[datuak egoera] (erak/ezabatu! erabiltzailea)]
          (json-erantzuna datuak egoera)))  
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (wrap-cors
       :access-control-allow-headers "Access-Control-Allow-Origin, X-Requested-With, Content-Type, Accept"
       :access-control-allow-origin #".*"
       :access-control-allow-methods [:get :put :post :delete])))
