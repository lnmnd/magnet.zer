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
  (GET "/v1/erabiltzaileak" []
       (json-erantzuna
        (let [era (sql/with-connection konfig/db-con
                    (sql/with-query-results res
                      ["select erabiltzailea, pasahitza, izena, deskribapena, sortze_data from erabiltzaileak"]
                      (doall res)))]
          (if era
            (let [erabiltzaileak (sql/with-connection konfig/db-con
                                   (sql/with-query-results res
                                     ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak desc"]
                                     (doall res)))]
              {:desplazamendua 0
               :muga 0
               :guztira (count erabiltzaileak)
               :erabiltzaileak erabiltzaileak})
            {:desplazamendua 0
             :muga 0
             :guztira 0
             :erabiltzaileak []}))))
  (GET "/v1/erabiltzaileak/:erabiltzailea" {{erabiltzailea :erabiltzailea} :params}
       (json-erantzuna {:erabiltzailea
                        (first (sql/with-connection konfig/db-con
                                 (sql/with-query-results res
                                   ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" erabiltzailea]
                                   (doall res))))}))
  (POST "/v1/erabiltzaileak" eskaera
        (let [edukia (json/parse-string (slurp (:body eskaera)) true)]
          (if (baliozko-erabiltzailea edukia)
            (do (sql/with-connection konfig/db-con
                  (sql/insert-values :erabiltzaileak
                                     [:erabiltzailea :pasahitza :izena :deskribapena :sortze_data]
                                     [(:erabiltzailea edukia) (pasahitz-hash (:pasahitza edukia)) (:izena edukia) (:deskribapena edukia) "TODO zehazteke"]))
                (json-erantzuna {:erabiltzailea
                                 (first (sql/with-connection konfig/db-con
                                          (sql/with-query-results res
                                            ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" (:erabiltzailea edukia)]
                                            (doall res))))}))
            (json-erantzuna {} 400))))
  (PUT "/v1/erabiltzaileak/:erabiltzailea" eskaera
       (let [erabiltzailea (:erabiltzailea (:params eskaera))
             edukia (json/parse-string (slurp (:body eskaera)) true)]
         (if (baliozko-erabiltzailea edukia)
           (do (sql/with-connection konfig/db-con
                 (sql/update-values :erabiltzaileak
                                    ["erabiltzailea=?" erabiltzailea]
                                    {:pasahitza (pasahitz-hash (:pasahitza edukia))
                                     :izena (:izena edukia)
                                     :deskribapena (:deskribapena edukia)
                                     :sortze_data "TODO zehazteke"}))
               (json-erantzuna {:erabiltzailea
                                (first (sql/with-connection konfig/db-con
                                         (sql/with-query-results res
                                           ["select erabiltzailea, izena, deskribapena, sortze_data from erabiltzaileak where erabiltzailea=?" erabiltzailea]
                                           (doall res))))}))
           (json-erantzuna {} 400))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (wrap-cors
       :access-control-allow-headers "Access-Control-Allow-Origin, X-Requested-With, Content-Type, Accept"
       :access-control-allow-origin #".*"
       :access-control-allow-methods [:get :put :post :delete])))
