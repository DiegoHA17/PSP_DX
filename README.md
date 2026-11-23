# Programación de Servicios y Procesos — UT1: Procesos y servicios

Prácticas entregables de la **Unidad 1** del módulo *Programación de Servicios y
Procesos* (0490), 2.º DAM, curso 2026-27.

El repositorio contiene un proyecto Gradle de consola con las prácticas de
procesos (2, 3 y 4), un proyecto Android con el servicio en primer plano
(práctica 5) y la documentación de las cinco prácticas.

## Prácticas

| N.º | Práctica | Ubicación | Documentación |
|:---:|----------|-----------|---------------|
| 1 | Planificación de procesos (FCFS, SJF, RR q=3, prioridades) | — (ejercicio teórico) | [`documentacion/practica1`](documentacion/practica1/README.md) |
| 2 | Gestión y monitorización de procesos | `procesos/.../practicas/BuscarProcesos.kt`, `procesos/.../ejemplos/Dormilon.kt` | [`documentacion/practica2`](documentacion/practica2/README.md) |
| 3 | Lanzador de órdenes | `procesos/.../practicas/Lanzador.kt` | [`documentacion/practica3`](documentacion/practica3/README.md) |
| 4 | Coordinador de procesos en paralelo | `procesos/.../practicas/Coordinador.kt` (+ `GeneradorDatos.kt`, `Contador.kt`) | [`documentacion/practica4`](documentacion/practica4/README.md) |
| 5 | Servicio en primer plano en Android | `ServicioPSP/app/src/main/java/es/dam/psp/ut1/` | [`documentacion/practica5`](documentacion/practica5/README.md) |

`procesos/...` abrevia `procesos/src/main/kotlin/es/dam/psp/ut1`.

## Estructura

```text
.
├── README.md
├── PENDIENTES_EVIDENCIAS.md        capturas y correcciones pendientes del documento
├── procesos/                       proyecto Gradle de consola (Kotlin/JVM)
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── src/main/kotlin/es/dam/psp/ut1/
│       ├── ejemplos/Dormilon.kt
│       └── practicas/
│           ├── BuscarProcesos.kt   práctica 2
│           ├── Lanzador.kt         práctica 3
│           ├── GeneradorDatos.kt   práctica 4 (no se modifica)
│           ├── Contador.kt         práctica 4 (no se modifica)
│           └── Coordinador.kt      práctica 4
├── ServicioPSP/                    proyecto Android (paquete es.dam.psp.ut1)
│   └── app/src/main/
│       ├── AndroidManifest.xml
│       └── java/es/dam/psp/ut1/
│           ├── MainActivity.kt
│           └── servicio/DescargaService.kt
└── documentacion/
    ├── UT-1_entrega_v1.pdf         documento entregado en la primera revisión
    └── practica1/ … practica5/     resultados, explicaciones y evidencias
```

## Tecnologías

- Kotlin 2.0 (JVM 21) con Gradle 8.5 (wrapper incluido) para las prácticas de consola.
- `ProcessBuilder` y `ProcessHandle` de la API estándar de Java.
- Android: Android Gradle Plugin 9.4, Gradle 9.6 (wrapper incluido),
  minSdk 24, compileSdk y targetSdk 37.

## Requisitos

- JDK 21 (`java -version`).
- IntelliJ IDEA para `procesos/` y Android Studio para `ServicioPSP/` (o solo la
  terminal con los wrappers `gradlew`).
- Para la práctica 5: Android SDK (platform 37) y un emulador o móvil con
  Android 7.0 (API 24) o superior.

## Ejecución de las prácticas de consola

Desde IntelliJ: abrir la carpeta `procesos/` y ejecutar el `main` de cada fichero
(el directorio de trabajo es `procesos/`).

Desde la terminal (en Windows usar `gradlew.bat` o `.\gradlew`):

```bash
cd procesos
./gradlew build                                                     # compilar

./gradlew run -Pmain=es.dam.psp.ut1.practicas.BuscarProcesosKt --args="java"
./gradlew run -Pmain=es.dam.psp.ut1.practicas.LanzadorKt           # escribir 'salir' para terminar
./gradlew run -Pmain=es.dam.psp.ut1.practicas.GeneradorDatosKt     # crea procesos/datos/
./gradlew run -Pmain=es.dam.psp.ut1.practicas.CoordinadorKt
./gradlew run -Pmain=es.dam.psp.ut1.practicas.CoordinadorKt --args="limitado"
./gradlew run -Pmain=es.dam.psp.ut1.ejemplos.DormilonKt --args="300"
```

`datos/` e `historial.txt` se generan en `procesos/` al ejecutar y no se
versionan.

## Ejecución de la práctica 5 (Android)

1. Abrir `ServicioPSP/` en Android Studio y sincronizar Gradle.
2. Ejecutar la configuración `app` en un emulador o móvil.
3. Aceptar el permiso de notificaciones y pulsar **Iniciar** / **Parar**.
4. Logcat con el filtro `tag:PSP-UT1`; procesos con `adb shell "ps -A | grep psp"`.

Desde la terminal: `cd ServicioPSP && ./gradlew assembleDebug`
(APK en `app/build/outputs/apk/debug/`).

## Resultados y evidencias

Cada carpeta `documentacion/practicaN/` resume la solución, los resultados
obtenidos y las respuestas a las preguntas del enunciado. Los registros de
ejecución incluidos son salidas reales; las capturas de pantalla exigidas (con el
usuario visible) están en el documento de entrega, y las que faltan o deben
rehacerse se enumeran en [`PENDIENTES_EVIDENCIAS.md`](PENDIENTES_EVIDENCIAS.md).

Las versiones anteriores del Lanzador y del Coordinador se conservan solo en el
historial de Git.

## Autor

Diego Herrera Ayuso — 2.º DAM.
