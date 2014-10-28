(ns magnet.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.cors :refer [wrap-cors]]
            [clj-json.core :as json]
            [magnet.konfig :as konfig]
            [magnet.erabiltzaileak :as erak]
            [magnet.saioak :as saioak]
            [magnet.liburuak :as liburuak]))

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
  (api-erantzuna GET "erabiltzaileak" eskaera
                 (let [{query-params :query-params} eskaera
                       muga (if (contains? query-params "muga")
                              (read-string (query-params "muga"))
                              konfig/muga)
                       desplazamendua (if (contains? query-params "desplazamendua")
                                        (read-string (query-params "desplazamendua"))
                                        0)]
                   (erak/lortu-bilduma desplazamendua (if (<= muga 100) muga 100))))
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
                   (saioak/hasi! edukia)))
  (api-erantzuna GET "saioak/:token" {{token :token} :params}
                 (saioak/lortu token))
  (api-erantzuna DELETE "saioak/:token" {{token :token} :params}
                 (saioak/amaitu! token))

  ; liburuak
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
  
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (wrap-cors
       :access-control-allow-headers "Access-Control-Allow-Origin, X-Requested-With, Content-Type, Accept"
       :access-control-allow-origin #".*"
       :access-control-allow-methods [:get :put :post :delete])))
