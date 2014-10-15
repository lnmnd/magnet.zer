# Magnet loturak: zerbitzariak

[![Build Status](https://travis-ci.org/lnmnd/magnet.zer.svg?branch=master)](https://travis-ci.org/lnmnd/magnet.zer)

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

Fitxategiak aldatu ahala probak exekutatzeko:
> => (autotest)

Komando lerrotik:
> $ lein midje
	    
Fitxategiak aldatu ahala probak exekutatzeko:
> $ lein midje :autotest