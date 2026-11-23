# UT1 — Procesos y servicios

**Estado:** evaluada y cerrada. Los problemas técnicos que siguen abiertos están
en [`documentacion/REVISION.md`](documentacion/REVISION.md).

Contenidos: planificación de procesos, creación y control de procesos con
`ProcessBuilder`/`ProcessHandle`, comunicación padre-hijo, procesos en paralelo y
servicios en primer plano en Android.

## Prácticas

| N.º | Práctica | Código | Documentación |
|:---:|----------|--------|---------------|
| 1 | Planificación de procesos (FCFS, SJF, RR q=3, prioridades) | — (ejercicio teórico) | [practica1](documentacion/practica1/README.md) |
| 2 | Gestión y monitorización de procesos | [`BuscarProcesos.kt`](procesos/src/main/kotlin/es/dam/psp/ut1/practicas/BuscarProcesos.kt), [`Dormilon.kt`](procesos/src/main/kotlin/es/dam/psp/ut1/ejemplos/Dormilon.kt) | [practica2](documentacion/practica2/README.md) |
| 3 | Lanzador de órdenes | [`Lanzador.kt`](procesos/src/main/kotlin/es/dam/psp/ut1/practicas/Lanzador.kt) | [practica3](documentacion/practica3/README.md) |
| 4 | Coordinador de procesos en paralelo | [`Coordinador.kt`](procesos/src/main/kotlin/es/dam/psp/ut1/practicas/Coordinador.kt) (+ `GeneradorDatos.kt`, `Contador.kt`) | [practica4](documentacion/practica4/README.md) |
| 5 | Servicio en primer plano en Android | [`MainActivity.kt`](ServicioPSP/app/src/main/java/es/dam/psp/ut1/MainActivity.kt), [`DescargaService.kt`](ServicioPSP/app/src/main/java/es/dam/psp/ut1/servicio/DescargaService.kt) | [practica5](documentacion/practica5/README.md) |

## Proyectos

```text
UT1/
├── procesos/        Gradle de consola · Kotlin 2.0 · JVM 21 · Gradle 8.5
│   └── src/main/kotlin/es/dam/psp/ut1/{ejemplos,practicas}/
├── ServicioPSP/     Android · AGP 9.4 · Gradle 9.6 · minSdk 24 · target/compileSdk 37
│   └── app/src/main/java/es/dam/psp/ut1/{MainActivity.kt, servicio/}
└── documentacion/   practica1..5, documento entregado y registro de la revisión
```

## Ejecución

Prácticas de consola. Desde IntelliJ: abrir `UT1/procesos/` y ejecutar el `main`
de cada fichero. Desde la terminal (en Windows, `.\gradlew`):

```bash
cd UT1/procesos
./gradlew build
./gradlew run -Pmain=es.dam.psp.ut1.practicas.BuscarProcesosKt --args="java"
./gradlew run -Pmain=es.dam.psp.ut1.practicas.LanzadorKt          # 'salir' para terminar
./gradlew run -Pmain=es.dam.psp.ut1.practicas.GeneradorDatosKt    # crea datos/
./gradlew run -Pmain=es.dam.psp.ut1.practicas.CoordinadorKt       # --args="limitado" para la mejora
./gradlew run -Pmain=es.dam.psp.ut1.ejemplos.DormilonKt --args="300"
```

`datos/` e `historial.txt` se generan en `UT1/procesos/` y no se versionan.

Servicio Android: abrir `UT1/ServicioPSP/` en Android Studio, ejecutar `app`,
aceptar el permiso de notificaciones y usar **Iniciar** / **Parar**. Logcat con
`tag:PSP-UT1`; procesos con `adb shell "ps -A | grep psp"`. Desde la terminal:
`cd UT1/ServicioPSP && ./gradlew assembleDebug`.
