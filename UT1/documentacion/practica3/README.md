# Práctica 3 — Lanzador de órdenes

Código: `procesos/src/main/kotlin/es/dam/psp/ut1/practicas/Lanzador.kt`
(única versión; las cuatro anteriores siguen en el historial de Git).

```bash
cd UT1/procesos
./gradlew run -Pmain=es.dam.psp.ut1.practicas.LanzadorKt
```

## Cómo resuelve cada apartado

| Apartado | Solución |
|----------|----------|
| a) salida numerada | `redirectErrorStream(true)` une stdout y stderr; al terminar el hijo se lee el fichero temporal y se numera cada línea |
| b) `[código X · Y ms]` | `exitValue()` del hijo y tiempo medido con `System.nanoTime()` desde `start()` |
| c) timeout | `waitFor(TIMEOUT_S, SECONDS)`; si vence: aviso, `destroyForcibly()` de los descendientes (obtenidos antes de matar al padre) y del proceso, y código **-1**. La salida va a un fichero temporal (`File.createTempFile`), así el padre nunca se bloquea leyendo una tubería llena |
| d) historial | Cada ejecución se añade a `procesos/historial.txt` como `orden;codigo;milisegundos` (un `;` dentro de la orden se cambia por `,` para no romper el formato) |

## e) Sesión de prueba y códigos de salida

Sesión real ejecutada el 23/11/2026 en Windows 11 (`cmd.exe /c`), `TIMEOUT_S = 5`:

```text
> echo Hola
1: Hola
[código 0 · 36 ms]
> type archivo_inexistente.txt
1: El sistema no puede encontrar el archivo especificado.
[código 1 · 53 ms]
> comando_que_no_existe
1: "comando_que_no_existe" no se reconoce como un comando interno o externo,
2: programa o archivo por lotes ejecutable.
[código 1 · 45 ms]
> ping -n 11 127.0.0.1
Tiempo límite de 5 s superado: se termina el proceso y sus descendientes.
1:
2: Haciendo ping a 127.0.0.1 con 32 bytes de datos:
3: Respuesta desde 127.0.0.1: bytes=32 tiempo<1m TTL=128
...
7: Respuesta desde 127.0.0.1: bytes=32 tiempo<1m TTL=128
[código -1 · 5111 ms]
```

`historial.txt` generado por esa sesión:

```text
echo Hola;0;36
type archivo_inexistente.txt;1;53
comando_que_no_existe;1;45
ping -n 11 127.0.0.1;-1;5111
```

Después del timeout no quedó ningún `PING.EXE` vivo (`tasklist | grep -i ping`).

| Orden | Código | Explicación |
|-------|-------:|-------------|
| `echo Hola` | 0 | Terminó bien |
| `type archivo_inexistente.txt` | 1 | `type` no encuentra el fichero y devuelve error |
| `comando_que_no_existe` | 1 | `cmd.exe` no reconoce la orden y termina con 1 (en Linux, `sh` devuelve **127**) |
| `ping -n 11 127.0.0.1` | **-1** | Tarda ~10 s y supera `TIMEOUT_S` = 5 s. El Lanzador mata el proceso y **asigna -1**, el código que pide el enunciado para el timeout |

### Código real del hijo frente al código del Lanzador

Al matar un proceso con `destroyForcibly()` el sistema sí le pone un código de
salida real (en Windows `TerminateProcess` deja **1**; en Linux, `SIGKILL` deja
**137**). Ese valor no indica qué ha pasado, por eso el Lanzador no lo usa: ante
un timeout muestra y guarda **-1**. Por eso la captura muestra `[código -1 · ...]`.

**Corrección para el documento de entrega:** el punto 4 decía «devuelve el
código 1». Debe decir que el Lanzador **termina el proceso y sus descendientes,
avisa al usuario y usa el código -1**, que es lo que aparece en la captura y en
`historial.txt`.
