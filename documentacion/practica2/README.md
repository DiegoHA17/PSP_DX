# Práctica 2 — Gestión y monitorización de procesos

Código: `procesos/src/main/kotlin/es/dam/psp/ut1/practicas/BuscarProcesos.kt`
(paquete `es.dam.psp.ut1.practicas`) y `.../ejemplos/Dormilon.kt`.

## c) BuscarProcesos

Recibe una cadena como argumento y muestra solo los procesos cuyo comando la
contiene (sin distinguir mayúsculas). Para cada uno: PID, PPID, usuario, hora de
inicio (`info().startInstant()`, en UTC) y tiempo total de CPU
(`info().totalCpuDuration()`, formato ISO-8601, p. ej. `PT0.625S` = 0,625 s).

```bash
cd procesos
./gradlew run -Pmain=es.dam.psp.ut1.practicas.BuscarProcesosKt --args="java"
```

Comprobado el 23/11/2026: con `java` lista los procesos `java.exe`/`jusched.exe`
con todas las columnas; sin argumento muestra `Uso: BuscarProcesos <cadena>`.
Cuando el sistema no permite leer un dato (procesos de otro usuario o del
sistema) se muestra `-`.

## Evidencias existentes (documento de entrega, págs. 4-6)

| Apartado | Evidencia | Estado |
|----------|-----------|--------|
| a) cinco procesos con más memoria | Captura del Administrador de tareas con PID y usuario | Hay captura. Falta una tabla con el PID de cada uno (34148 IntelliJ, 26476 Claude, 35448 Chrome, 21208 Notion, 20792 Node) y si es aplicación o servicio |
| b) árbol de Dormilon | Captura de Dormilon (PID 6552) y de `Get-CimInstance` (PPID 13224) | Completar la cadena PID → PPID hasta `idea64.exe` y dibujar el árbol |
| c) BuscarProcesos | Captura de la salida buscando `java` | Correcta. Si se repite, hacerlo con la clase nueva `BuscarProcesosKt` |
| d) prioridad baja | Captura del Administrador de tareas con prioridad «Baja» | Correcta. Conviene que se vea la orden `start /low java -cp ... es.dam.psp.ut1.ejemplos.DormilonKt 120` |

Las capturas no se han rehecho ni simulado; ver `PENDIENTES_EVIDENCIAS.md`.
