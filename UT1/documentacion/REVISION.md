# UT1 — Registro de la revisión

La UT1 está **evaluada y cerrada**: no se van a rehacer entregas, capturas ni
mediciones. Este documento deja constancia de cómo llegó el repositorio a su
estado actual y de los problemas técnicos que siguen abiertos.

## Cronología

| Momento | Qué ocurrió |
|---------|-------------|
| Primera entrega | Un único commit (`608c2d0`) con todo el código y el documento [`UT-1_entrega_v1.pdf`](UT-1_entrega_v1.pdf). |
| Revisión del profesor | Observaciones: cuatro versiones del Lanzador y seis del Coordinador sueltas; un solo commit; timeout documentado como `1` en vez de `-1`; `GeneradorDatos` y `Contador` modificados; falta explicar la aceleración; paquete `com.example.ut1`; logs sin PID ni hilo; faltan evidencias de 5 d) y e). |
| 23/11/2026 | Siete commits de corrección realizados **el mismo día**, después de la entrega (no reflejan el desarrollo original, que está en `608c2d0`). |
| Después | Reorganización del repositorio por unidades (`UT1/`). El código evaluado no se modificó. |

## Correcciones realizadas

| Observación | Commit | Resultado |
|-------------|--------|-----------|
| Estructura desordenada y archivos generados versionados | `00bfe16` | Proyectos `procesos/` y `ServicioPSP/`, `.gitignore`, sin `.idea` ni `datos/` |
| BuscarProcesos y Dormilon fuera de su paquete | `a7084e7` | `es.dam.psp.ut1.practicas` / `.ejemplos` |
| Cuatro Lanzadores; timeout `1` frente a `-1` | `b2cb663` | Un Lanzador; timeout siempre `-1` ([práctica 3](practica3/README.md)) |
| Seis Coordinadores | `d1fc2d1` | Un Coordinador con la mejora opcional ([práctica 4](practica4/README.md)) |
| Paquete `com.example.ut1`; logs sin PID ni hilo | `fa7f1f8`, `533af05` | `es.dam.psp.ut1` y `.servicio`; tag `PSP-UT1` con PID e hilo; dos carreras del servicio corregidas ([práctica 5](practica5/README.md)) |
| Falta la explicación de la aceleración | `b341203` | Explicación con mediciones reales ([práctica 4](practica4/README.md)) |

## Problemas técnicos que siguen abiertos

1. **`GeneradorDatos.kt` y `Contador.kt` no son los originales del profesor.**
   El generador usa `Random.Default` sin semilla. Los originales (carpeta
   `Codigo_practicas`) no estaban disponibles y no se inventaron: los ficheros se
   conservaron tal cual. En consecuencia, las mediciones de la práctica 4 se
   hicieron con datos que no son comparables con los de clase.
2. **El documento de entrega no se corrigió.** El PDF conserva los errores
   detectados en la revisión: columna «Retorno medio» de la práctica 1 (contiene
   tiempos de respuesta), timeout descrito con código `1`, captura con
   `ClassNotFoundException` en la práctica 4 y capturas de la práctica 5 hechas
   con el paquete antiguo. Las versiones corregidas de esas explicaciones están
   en `practica1/` a `practica5/`.
3. **Capturas no rehechas.** No existen capturas con el usuario visible de la
   versión final de las prácticas 3, 4 y 5 (notificación con progreso, Logcat
   `tag:PSP-UT1` y `adb shell "ps -A | grep psp"`). Los registros de texto de
   `practica3/` a `practica5/` son salidas reales de la verificación, no
   capturas.
4. **Lint de Android sin ejecutar.** La compilación y los tests unitarios
   pasan, pero `lintDebug` no se pudo ejecutar sin conexión.
