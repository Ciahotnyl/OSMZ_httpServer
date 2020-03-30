# OSMZ

## Autor: Lukáš Ciahotný, CIA0008
## Projekt: HttpServer
## Datum: 30.3.2020

### Testováno na: Samsung Galaxy A6+
### Soubory pro web-server by měly být uploadované do složky: `/sdcard/Picture`
### Testovací soubory je možné najít ve složce `DataFiles`

### Snapshot -> Pro testování jsem vytvořil soubor `stream.html`, který se nachází ve složce `DataFiles`. Jinak volání přes `/camera/snapshot`
### Stream -> `/camera/stream`
### CGI-BIN -> `/cgi-bin/` kde následuje command a argumenty se oddělují znakem `%`. Testováno na `/cgi-bin/cal%2020`, `/cgi-bin/uptime` `/cgi-bin/cat%/proc/cpuinfo`
### Překlopit server do podoby služby se mi nepodařilo

