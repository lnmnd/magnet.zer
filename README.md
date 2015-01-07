# Magnet loturak: zerbitzaria

[![Build Status](https://travis-ci.org/lnmnd/magnet.zer.svg?branch=master)](https://travis-ci.org/lnmnd/magnet.zer)

## Dependentziak
- JDK 7 edo berriagoa (OpenJDk edo Oracle)
- [Leiningen](http://leiningen.org/)

## Konfigurazioa
Zerbitzaria martxan jartzean eta probak burutzean konfigurazioa konfig.clj eta proba-konfig.clj fitxategietatik irakurtzen dira, hurrenez hurren. Horiek ez daude eta ez dira bertsio kontrolean sartu behar, beraz, norberak sortu behar ditu. Horretarako konfig.adb.clj eta proba-konfig.adb.clj erabili daitezke eredu gisa.

Bestalde, datu-basea hasieratu behar da. Horretarako "hasieratu" argumentua pasa behar zaio aplikazioari:

> $ lein run hasieratu

REPL irekita:

> => (magnet.lagun/db-hasieratu (:db-kon konfig))

edo banaketako jar fitxategia edukiz gero:

> $ java -jar magnet-1.0.0-standalone.jar hasieratu

## Zerbitzaria martxa jarri
### REPL bidez
REPL abiarazi
> $ lein repl

Lehenik zerbitzaria sortu:
> => (sortu)

Eta gero hasi:
> => (hasi)

Geratzeko:
> => (geratu)

Edo Berrabiarazi nahi bada:
> => (berrabiarazi)

konfig aldagaian konfigurazioa gordetzen da eta zer aldagaian zerbitzaria. Hori jakinda konfigurazio desberdina duen beste zerbitzari bat sortzea posible da:
> => (def k-3001 (assoc konfig :portua 3001))

> => (def zer-3001 (z/sortu k-3001 (handler-sortu k-3001 (saioak-sortu))))

> => (z/hasi zer-3001) ; 3001 portuan hasiko da

> => (def k-ccc (assoc konfig :trackerrak ["udp://tracker.ccc.de:80"]))

> => (def zer-ccc (z/sortu k-ccc (handler-sortu k-ccc (saioak-sortu))))

> => (z/hasi zer-ccc) ; CCC trackerra bakarrik erabiliko du

### Komando lerrotik
> $ lein run

Geratzeko prozesua amaitu, Ctl+C erabiliz adibidez.

## Probak

### REPL barnetik
Proba denak exekutatu:
> => (use 'midje.repl)

> => (load-facts)

Etiketa jakin bat dutenak:
> => (load-facts :saioak)

### Komando lerrotik
> $ lein midje
	    
Proba guztiak exekutatzea nahiko makala da. 

## Kodearen dokumentazioa
> $ lein doc

doc katalogoan dokumentazioa sortzen du.

## Banaketa prestatu
> $ lein uberjar

*target* katalogoan 2 JAR egongo dira, *standalone*-k dependentzi guztiak ditu.

Exekutatzeko:
> $ java -jar target/magnet-1.0.0-standalone.jar