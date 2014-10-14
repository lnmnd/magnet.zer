# Magnet loturak: zerbitzariak

Osatzeko

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
> => (autotest)

Komando lerrotik:
> $ lein midje
	    
Fitxategiak aldatu ahala probak exekutatzeko:
> $ lein midje :autotest