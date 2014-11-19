# Magnet loturak: zerbitzariak

[![Build Status](https://travis-ci.org/lnmnd/magnet.zer.svg?branch=master)](https://travis-ci.org/lnmnd/magnet.zer)

## Dependentziak
JDK 7 edo berriagoa, *Java NIO* bertsio horretan gehitu baitzen.

## Zerbitzaria martxa jarri

REPL bidez:
> => (magnet.handler.main/zer-hasi 3000)

Geratzeko:
> => (magnet.handler.main/zer-geratu)

Komando lerrotik:
> $ lein ring server-headless

http://localhost:3000 helbidean egongo da.

## Probak

REPL barnetik:
> => (use 'midje.repl)

Proba denak exekutatu:
> => (load-facts)

Etiketa jakin bat dutenak
> => (load-facts :saioak)

Fitxategiak aldatu ahala probak exekutatzeko:
> => (autotest)

Komando lerrotik:
> $ lein midje
	    
Fitxategiak aldatu ahala probak exekutatzeko:
> $ lein midje :autotest

## Kodearen dokumentazioa
> $ lein doc

doc katalogoan dokumentazioa sortzen du.

## JAR fitxategia sortu
> $ lein ring uberjar

*target* katalogoan 2 JAR egongo dira, *standalone* duenak dependentzi guztiak ditu.

Exekutatzeko:
> $ java -jar sortutakoa.jar [portua]

Portu zenbakia aukerazkoa da, 8080 lehenetsia izanik.