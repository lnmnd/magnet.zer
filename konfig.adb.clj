{:portua 3000
 ;; Datubasearen konexio parametroak, dbspec gisa.
 :db-kon
 {:classname "org.h2.Driver"
  :subprotocol "h2"
  :subname "magnet"}
 ;; Bilduma batean agertuko diren elementu kopurua.
 :muga 25
 ;; Saioaren iraungitze denbora, segundotan.
 :saio-iraungitze-denbora (* 60 60) ; 1 h
 ;; Torrentak partekatu nahi diren edo ez.
 :partekatu true
 :kokapenak {:publikoa "files/public"
             :epub-karpeta "files/private/torrent/"
             :torrent-karpeta "files/private/torrent/"
             :irudi-karpeta "files/public/img/"
             :irudi-url "http://localhost:3000/img/"}   
 ;; Trackerren zerrenda.
 ;; Lehenengoa torrentaren tracker gisa ezarriko da.
 ;; Gainontzekoak magnet loturak agertuko dira.
 :trackerrak ["udp://pi:6969"
              "udp://tracker.publicbt.com:80"
              "udp://tracker.istole.it:6969"
              "udp://tracker.ccc.de:80"]
 ;; Torrent fitxategia eta katalogoa emanda torrenta partekatze duen programa.
 :torrent-gehitze-programa
 (fn [sh torrent katalogoa]
   (sh "transmission-remote" "--add" torrent "-w" katalogoa))}
