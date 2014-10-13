(ns magnet.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.java.jdbc :as sql]
            [clj-json.core :as json]))

(defn json-erantzuna
  "Datuak JSON formatuan itzultzen ditu. Egoera aukeran, 200 lehenetsia."
  [datuak & [egoera]]
  {:status (or egoera 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string datuak)})

(defroutes app-routes
  (GET "/" []
       (json-erantzuna [{:id 1 :titulua "bilduma bat"}
                        {:id 2 :titulua "adibide bat"}
                        {:id 3 :titulua "besterik ez"}]))
  ; zerbait itzuli
  (GET "/v1/saioak/:token" []
       (json-erantzuna {:erabiltzailea "erab1"
                        :saio_hasiera "oraindik ez"
                        :iraungitze_data "oraindik ez"}))

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (wrap-cors
       :access-control-allow-headers "Access-Control-Allow-Origin, X-Requested-With, Content-Type, Accept"
       :access-control-allow-origin #".*"
       :access-control-allow-methods [:get :put :post :delete])))
