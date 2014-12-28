(ns ^{:doc "Domeinua: iruzkinak."}
  magnet.iruzkinak
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [magnet.lagun :refer [oraingo-data orriztatu]]
            [magnet.saioak :refer [lortu-saioa]]))

(defn- gehitu-iruzkina!
  [kon edukia]
  (let [ir (assoc edukia :data (oraingo-data))]
    (do (sql/insert! kon :iruzkinak
                     [:liburua :erabiltzailea :data :edukia]
                     [(:liburua ir) (:erabiltzailea ir) (:data ir) (:edukia ir)])
        (let [id (:id (first (sql/query kon "select identity() as id")))]
          (doseq [gur (:gurasoak ir)]
            (sql/insert! kon :iruzkin_erantzunak
                         [:gurasoa :erantzuna]
                         [gur id]))
          (assoc ir :id id)))))

(defn- lortu-iruzkina
  [kon id]
  (if-let [ir (first (sql/query kon ["select * from iruzkinak where id=?" id]))]
    (assoc ir
      :gurasoak (map :gurasoa (sql/query kon ["select gurasoa as gurasoa from iruzkin_erantzunak where erantzuna=?" id]))
      :erantzunak (map :erantzuna (sql/query kon ["select erantzuna as erantzuna from iruzkin_erantzunak where gurasoa=?" id])))
    nil))

(defn- aldatu-iruzkina!
  [kon id edukia]
  (sql/update! kon :iruzkinak
               {:edukia (:edukia edukia)}
               ["id=?" id]))

(defn- ezabatu-iruzkina!
  [kon id]
  (sql/delete! kon :iruzkinak ["id=?" id]))

(defn- iruzkinak [db-kon idak]
  (pmap (fn [x] (lortu-iruzkina db-kon (:id x))) idak))

(defn gehitu!
  "id liburuarekin lotutako iruzkina gehitu."
  [saio-osa db-kon token id edukia]
  (sql/with-db-transaction [kon db-kon]
    (if-let [{erabiltzailea :erabiltzailea} (lortu-saioa saio-osa token)]
      [:ok {:iruzkina
            (gehitu-iruzkina! kon (assoc edukia
                                    :liburua id
                                    :erabiltzailea erabiltzailea
                                    :erantzunak []))}]
      [:baimenik-ez])))

(defn aldatu!
  "Iruzkinaren edukia aldatzen du."
  [saio-osa db-kon token id edukia]
  (sql/with-db-transaction [kon db-kon]
    (if-let [ir (lortu-iruzkina kon id)]
      (if (= (:erabiltzailea (lortu-saioa saio-osa token))
             (:erabiltzailea ir))
        (do (aldatu-iruzkina! kon id edukia)
            [:ok {:iruzkina (assoc ir :edukia (:edukia edukia))}])
        [:baimenik-ez])
      [:ez-dago])))

(defn lortu
  "id jakineko iruzkina lortzen du."
  [db-kon id]
  (sql/with-db-connection [kon db-kon]
    (if-let [ir (lortu-iruzkina kon id)]
      [:ok {:iruzkina ir}]
      [:ez-dago])))

(defn ezabatu!
  "Iruzkina ezabatzen du."
  [saio-osa db-kon token id]
  (sql/with-db-transaction [kon db-kon]
    (if-let [ir (lortu-iruzkina kon id)]
      (if (= (:erabiltzailea (lortu-saioa saio-osa token))
             (:erabiltzailea ir))
        (do (ezabatu-iruzkina! kon id)
            [:ok])
        [:baimenik-ez])
      [:ez-dago])))

(defn lortu-bilduma
  "Iruzkinen bilduma lortu"
  [desplazamendua muga db-kon]
  (sql/with-db-connection [kon db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from iruzkinak"]))
          idak (sql/query kon (orriztatu ["select id from iruzkinak"] desplazamendua muga))]
      [:ok {:desplazamendua desplazamendua
            :muga muga
            :guztira guztira
            :iruzkinak (iruzkinak db-kon idak)}])))

(defn lortu-liburuarenak
  "Liburu baten iruzkinak lortzen ditu."
  [desplazamendua muga db-kon id]
  (sql/with-db-connection [kon db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from iruzkinak where liburua=?" id]))
          idak (sql/query kon (orriztatu ["select id from iruzkinak where liburua=?" id] desplazamendua muga))]
      [:ok {:desplazamendua desplazamendua
            :muga muga
            :guztira guztira
            :iruzkinak (iruzkinak db-kon idak)}])))

(defn lortu-erabiltzailearenak
  "Erabiltzaile baten iruzkinak lortzen ditu."
  [desplazamendua muga db-kon era]
  (sql/with-db-connection [kon db-kon]
    (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from iruzkinak where erabiltzailea=?" era]))
          idak (sql/query kon (orriztatu ["select id from iruzkinak where erabiltzailea=?" era] desplazamendua muga))]
      [:ok {:desplazamendua desplazamendua
            :muga muga
            :guztira guztira
            :iruzkinak (iruzkinak db-kon idak)}])))
