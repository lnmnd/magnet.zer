(ns magnet.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.java.jdbc :as sql]
            [clj-json.core :as json]
            [magnet.konfig :as konfig]))

(defn json-erantzuna
  "Datuak JSON formatuan itzultzen ditu. Egoera aukeran, 200 lehenetsia."
  [datuak & [egoera]]
  {:status (or egoera 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string datuak)})

(defn baliozko-erabiltzailea
  "true baliozkoa bada. TODO oraingoz ezer ez"
  [erabiltzailea]
  true)

(defn pasahitz-hash
  "Pasahitzaren hash-a lortzen du. TODO oraingoz ezer ez"
  [pas]
  pas)

(defroutes app-routes
  (GET "/" []
       (json-erantzuna [{:id 1 :titulua "bilduma bat"}
                        {:id 2 :titulua "adibide bat"}
                        {:id 3 :titulua "besterik ez"}]))

  (GET "/v1/erabiltzaileak" []
       (json-erantzuna
        (let [era (sql/with-connection konfig/db-con
                    (sql/with-query-results res
                      ["select erabiltzailea, pasahitza, izena, deskribapena, sortze_data from erabiltzaileak"]
                      (doall res)))]
          (if era era
            {:desplazamendua 0
             :muga 0
             :guztira 0
             :erabiltzaileak []}))))
  (POST "/v1/erabiltzaileak" eskaera
        (let [edukia (json/parse-string (slurp (:body eskaera)) true)]
          (if (baliozko-erabiltzailea edukia)
            (do (sql/with-connection konfig/db-con
                  (sql/insert-values :erabiltzaileak
                                     [:erabiltzailea :pasahitza :izena :deskribapena :sortze_data]
                                     [(:erabiltzailea edukia) (pasahitz-hash (:pasahitza edukia)) (:izena edukia) (:deskribapena edukia) "TODO zehazteke"]))
                (json-erantzuna {}))
            (json-erantzuna {} 400))))

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (wrap-cors
       :access-control-allow-headers "Access-Control-Allow-Origin, X-Requested-With, Content-Type, Accept"
       :access-control-allow-origin #".*"
       :access-control-allow-methods [:get :put :post :delete])))
