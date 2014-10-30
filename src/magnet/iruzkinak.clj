(ns magnet.iruzkinak
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql]
            [magnet.lagun :refer [trafun oraingo-data]]
            [magnet.saioak :refer [lortu-saioa]]
            [magnet.konfig :as konfig]))

(defn- gehitu-iruzkina!
  [kon edukia]
  (let [ir (assoc edukia :data (oraingo-data))]
    (do (sql/insert! kon :iruzkinak
                       [:liburua :erabiltzailea :data :edukia]
                       [(:liburua edukia) (:erabiltzailea edukia) (:data edukia) (:edukia edukia)])
          (assoc edukia :id (:id (first (sql/query kon "select identity() as id")))))))

(defn- lortu-iruzkina
  [kon id]
  (first (sql/query kon ["select * from iruzkinak where id=?" id])))

(defn- aldatu-iruzkina!
  [kon id edukia]
  (sql/update! @konfig/db-kon :iruzkinak
               {:edukia (:edukia edukia)}
               ["id=?" id]))

(defn- ezabatu-iruzkina!
  [kon id]
  (sql/delete! kon :iruzkinak ["id=?" id]))

(trafun
 kon
 gehitu!
 "id liburuarekin lotutako iruzkina gehitu."
 [token id edukia]
 (if-let [{erabiltzailea :erabiltzailea} (lortu-saioa token)]
   [{:iruzkina
     (gehitu-iruzkina! kon (assoc edukia
                             :liburua id
                             :erabiltzailea erabiltzailea))}
    200]
   [{} 401]))

(trafun
 kon
 aldatu!
 "Iruzkinaren edukia aldatzen du."
 [token id edukia]
 (if-let [ir (lortu-iruzkina kon id)]
   (if (= (:erabiltzailea (lortu-saioa token))
          (:erabiltzailea ir))
     (do (aldatu-iruzkina! kon id edukia)
         [{:iruzkina (assoc ir :edukia (:edukia edukia))}
          200])
     [{} 401])
   [{} 404]))

(trafun
 kon
 lortu
 "id jakineko iruzkina lortzen du."
 [id]
 (if-let [ir (lortu-iruzkina kon id)]
   [{:iruzkina ir} 200]
   [{} 404]))

(trafun
 kon
 ezabatu!
 "Iruzkina ezabatzen du."
 [token id]
 (if-let [ir (lortu-iruzkina kon id)]
   (if (= (:erabiltzailea (lortu-saioa token))
          (:erabiltzailea ir))
     (do (ezabatu-iruzkina! kon id)
         [{} 200])
     [{} 401])
   [{} 404]))

(trafun
 kon
 lortu-bilduma
 "Iruzkinen bilduma lortu"
 [desplazamendua muga]
 (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from iruzkinak"]))
       irak (sql/query kon ["select id, liburua, erabiltzailea, data, edukia from iruzkinak limit ? offset ?" muga desplazamendua])]
      [{:desplazamendua desplazamendua
        :muga muga
        :guztira guztira
        :iruzkinak irak}
       200]))

(trafun
 kon
 lortu-liburuarenak
 "Liburu baten iruzkinak lortzen ditu."
 [id desplazamendua muga]
 (let [{guztira :guztira} (first (sql/query kon ["select count(*) as guztira from iruzkinak where liburua=?" id]))
       irak (sql/query kon ["select id, liburua, erabiltzailea, data, edukia from iruzkinak where liburua=? limit ? offset ?" id muga desplazamendua])]
      [{:desplazamendua desplazamendua
        :muga muga
        :guztira guztira
        :iruzkinak irak}
       200]))
