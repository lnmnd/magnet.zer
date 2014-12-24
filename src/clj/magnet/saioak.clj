(ns magnet.saioak
  (:require [clojure.java.jdbc :as sql]
            [clj-bcrypt-wrapper.core :refer [check-password]]
            [magnet.lagun :refer [oraingo-data segunduak-gehitu]]))

(defn- gehitu-saioa!
  "Saioa saioen zerrendan sartzen du."
  [saioak saio-iraungitze-denbora saioa]
  (swap! saioak conj {(:token saioa) saioa})
  (future (Thread/sleep (* saio-iraungitze-denbora 1000))
          (swap! saioak :dissoc (:token saioa))))

(defmacro ^:private zerrendatu [s]
  (into [] (re-seq #"[A-Z,0-9]" s)))

(defn- ausazko-hizkia []
  (rand-nth (zerrendatu "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")))

(defn- sortu-tokena
  "Zenbaki eta hizki larriz osatutako 32 luzerako ausazko tokena sortzen du."
  []
  (->> (for [x (range 32)]
         (ausazko-hizkia))
       (reduce str "")))

(defn- erabiltzaile-zuzena?
  "true erabiltzailea eta pasahitza zuzenak badira."
  [db-kon erabiltzailea pasahitza]
  (if-let [{pasahitz_hash :pasahitza} (first (sql/query db-kon ["select pasahitza from erabiltzaileak where erabiltzailea=?" erabiltzailea]))]
    (check-password pasahitza pasahitz_hash)
    false))

(defn saioak-sortu
  "Saioak sortzen du. Gainontzeko funtzioek datu hau erabiltzen dute."
  []
  (atom {}))

(defn hasi!
  "Erabiltzailea eta pasahitza zuzenak badira saioa hasten du."
  [saioak db-kon saio-iraungitze-denbora erabiltzailea pasahitza]
  (if (erabiltzaile-zuzena? db-kon erabiltzailea pasahitza)
    (let [orain (oraingo-data)
          saioa {:erabiltzailea erabiltzailea
                 :token (sortu-tokena)
                 :saio_hasiera orain
                 :iraungitze_data (segunduak-gehitu orain saio-iraungitze-denbora)}]
      (gehitu-saioa! saioak saio-iraungitze-denbora saioa)
      [:ok saioa]) 
    [:ezin-prozesatu]))

(defn amaitu!
  "Saioa amaitzen du, tokena baliogabetuz."
  [saioak token]
  (swap! saioak dissoc token)
  [:ok])

(defn lortu-saioa
  "Tokena duen saioa lortzen du.
\"lortu\" bezala baina saioa-egoera bikotea ordez saioa soilik itzultzen du edo false tokena ez badago."
  [saioak token]
  (if (contains? @saioak token)
    (@saioak token)
    false))
