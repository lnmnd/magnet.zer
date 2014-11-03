(require  '[clojure.string :as str]
          '[clojure.java.jdbc :as sql]
          '[magnet.lagun :refer [db-hasieratu db-garbitu oraingo-data]]
          '[magnet.erabiltzaileak :as erak]
          '[magnet.saioak :as saioak]
          '[magnet.liburuak :as libuk]
          '[magnet.iruzkinak :as iruzk]
          '[magnet.konfig :as konfig])

(db-garbitu)
(db-hasieratu)

(def era1 (erak/gehitu! {:erabiltzailea "era1"
                         :pasahitza "1111"
                         :izena "era1"}))
(def era2 (erak/gehitu! {:erabiltzailea "era2"
                         :pasahitza "2222"
                         :izena "era2"}))
(erak/lortu-bilduma 0 10)

(def saio1 (second (saioak/hasi! "era1" "1111")))
(def saio2 (second (saioak/hasi! "era2" "2222")))

(def lib1 (:liburua (second (libuk/gehitu! (:token saio1)
                                           {:epub "base64"
                                            :titulua "liburu 1"
                                            :egileak ["Joxe" "Patxi"]
                                            :hizkuntza "euskara"                 
                                            :sinopsia "Duela urte asko..."
                                            :urtea "2009"
                                            :etiketak ["kaixo" "joxe" "zaharra"]
                                            :azala "base64"}))))
(def lib2 (:liburua (second (libuk/gehitu! (:token saio1)
                                           {:epub "base64"
                                            :titulua "liburu 2"
                                            :egileak ["Joxe" "Patxi"]
                                            :hizkuntza "euskara"                 
                                            :sinopsia "Duela urte asko..."
                                            :urtea "2009"
                                            :etiketak ["kaixo" "joxe" "zaharra"]
                                            :azala "base64"}))))

(def ir1 (:iruzkina (second (iruzk/gehitu! (:token saio1) (:id lib1) {:gurasoak []
                                                                          :edukia "Iruzkin bat"}))))
(def ir2 (:iruzkina (second (iruzk/gehitu! (:token saio1) (:id lib1) {:gurasoak [(:id ir1)]
                                                                          :edukia "Iruzkin bat-i erantzuna"}))))
(def ir3 (:iruzkina (second (iruzk/gehitu! (:token saio1) (:id lib1) {:gurasoak [(:id ir1) (:id ir2)]
                                                                          :edukia "Beste erantzuna"}))))
(iruzk/lortu (:id ir1))
(iruzk/lortu-bilduma 0 10)

