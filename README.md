# Magnet loturak: zerbitzariak

[![Build Status](https://travis-ci.org/lnmnd/magnet.zer.svg?branch=master)](https://travis-ci.org/lnmnd/magnet.zer)

## Dependentziak
JDK 7 edo berriagoa, *Java NIO* bertsio horretan gehitu baitzen.

## Zerbitzaria martxa jarri

### REPL bidez
Hasteko:
> => (hasi)

Geratzeko:
> => (geratu)

Zerbitzaria zer aldagaian gordeta dago, norberak sortu dezake:
> => (def zer (z/sortu konfig (handler-sortu konfig)))
> => (z/hasi zer)
> => (z/geratu zer)

Beste zerbitzari bat sortzea posible da konfigurazio ezberdina erabiliz:
> => (def zer2 (z/sortu (assoc konfig :portua 3001) (handler-sortu konfig)))
> => (z/hasi zer2) ; 3001 portuak hasiko da

### Komando lerrotik
> $ lein run

http://localhost:3000 helbidean egongo da.

Geratzeko prozesua amaitu, Ctl+C erabiliz adibidez.

## Probak

### REPL barnetik
Proba denak exekutatu:
> => (use 'midje.repl)
> => (load-facts)

Etiketa jakin bat dutenak:
> => (load-facts :saioak)

Fitxategiak aldatu ahala probak exekutatzeko:
> => (autotest)

### Komando lerrotik
> $ lein midje
	    
Fitxategiak aldatu ahala probak exekutatzeko:
> $ lein midje :autotest

Proba guztiak exekutatzea nahiko makala da (2 m eta 50 s inguru 2x1400MHz-ko CPUarekin).

## Kodearen dokumentazioa
> $ lein doc

doc katalogoan dokumentazioa sortzen du.

## JAR fitxategia sortu
> $ lein ring uberjar

*target* katalogoan 2 JAR egongo dira, *standalone* duenak dependentzi guztiak ditu.

Exekutatzeko:
> $ java -jar sortutakoa.jar [portua]

Portu zenbakia aukerazkoa da, 3000 lehenetsia izanik.