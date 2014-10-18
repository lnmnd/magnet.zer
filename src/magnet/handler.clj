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

(defmacro api-erantzuna
  "Metodo, url, parametro eta edukia emanik erantzuna osatzen du"
  [metodoa url params edukia]
  `(~metodoa ~(str "/v1/" url) ~params
       (let [[datuak# egoera#] ~edukia]
         (json-erantzuna datuak# egoera#))))

(defroutes app-routes
  (api-erantzuna GET "erabiltzaileak" []
                 (erak/lortu-bilduma))
  (api-erantzuna GET "erabiltzaileak/:erabiltzailea" {{erabiltzailea :erabiltzailea} :params}
                 (erak/lortu erabiltzailea))
  (api-erantzuna POST "erabiltzaileak" eskaera
                 (let [edukia (json/parse-string (slurp (:body eskaera)) true)]
                   (erak/gehitu! edukia)))
  (api-erantzuna PUT "erabiltzaileak/:erabiltzailea" eskaera
                 (let [erabiltzailea (:erabiltzailea (:params eskaera))
                       edukia (json/parse-string (slurp (:body eskaera)) true)]
                   (erak/aldatu! erabiltzailea edukia)))
  (api-erantzuna DELETE "erabiltzaileak/:erabiltzailea" {{erabiltzailea :erabiltzailea} :params}
                 (erak/ezabatu! erabiltzailea))  
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (wrap-cors
       :access-control-allow-headers "Access-Control-Allow-Origin, X-Requested-With, Content-Type, Accept"
       :access-control-allow-origin #".*"
       :access-control-allow-methods [:get :put :post :delete])))
