# Evidencias de la práctica guiada

Salidas completas de ejecuciones reales y respuestas de la IA, guardadas tal
cual. Las ejecuciones las hizo Claude Code (coordinador) en la terminal
(`./gradlew run`, JDK 21, Windows 11), no en IntelliJ. Los prompts, las
respuestas y los diffs son de Codex (asistente de programación). Cada fichero de
ejecución empieza con la fecha, el usuario, la rama y las líneas `fabrica` /
`fabricaEspera` que había en ese momento (excepto `A1`, que se guardó antes de
añadir esa cabecera). Las capturas de IntelliJ van en la hoja de `../entrega/`,
que no se sube al repositorio (pasos en `../PENDIENTES.md`).

## Reto A · `TaquillaIA`

| Fichero | Contenido |
|---------|-----------|
| `retoA/A1_TaquillaIA.txt` … `A3_TaquillaIA.txt` | Las tres ejecuciones de la tabla de reglas rotas |
| `retoA/codex_auditoria.txt` | Auditoría de Codex en modo solo lectura (apartado c) |

## Reto B · `TaquillaSegura` (código de Codex)

| Fichero | Contenido |
|---------|-----------|
| `retoB/codex_respuesta.txt`, `retoB/codex_TaquillaSegura.patch` | Respuesta de Codex al prompt del Reto B y su diff (solo `TaquillaSegura.kt`) |
| `retoB/B1_TaquillaSegura.txt` … `B3_TaquillaSegura.txt` | Tres ejecuciones con R1-R5 OK; también son la mitad `TaquillaSegura` de la tabla de tiempos |
| `retoB/tiempos/T1_TaquillaIA.txt` … `T3_TaquillaIA.txt` | Mitad `TaquillaIA` de la tabla de tiempos, justo antes de `B1`-`B3` |

## Reto C · `TaquillaEspera` (código de Codex)

| Fichero | Contenido |
|---------|-----------|
| `retoC/codex_respuesta.txt`, `retoC/codex_TaquillaEspera.patch` | Respuesta de Codex al prompt del Reto C y su diff (solo `TaquillaEspera.kt`) |
| `retoC/C1_TaquillaEspera.txt` … `C3_TaquillaEspera.txt` | Tres ejecuciones con E1-E6 OK |
| `retoC/C1_if_roto.txt` … `C3_if_roto.txt` | Rota a propósito (`while` → `if`): E3 rota en 4/5 rondas y `NoSuchElementException` en los hilos que esperan. Después se restauró el `while` |
| `retoC/C_wait50.txt` | Prueba con `wait(50)`: E1 rota en 5/5 rondas (apartado d). Después se restauró `wait()` |
| `retoC/codex_notify.txt` | Respuesta de Codex sobre `notify()` frente a `notifyAll()` (apartado e) |
