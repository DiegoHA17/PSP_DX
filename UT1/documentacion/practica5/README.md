# Práctica 5 — Servicio en primer plano en Android

Proyecto Android: `ServicioPSP/` (Kotlin, minSdk 24, compileSdk/targetSdk 37).

| Elemento | Ubicación |
|----------|-----------|
| Paquete / `namespace` / `applicationId` | `es.dam.psp.ut1` |
| Actividad | `app/src/main/java/es/dam/psp/ut1/MainActivity.kt` |
| Servicio | `app/src/main/java/es/dam/psp/ut1/servicio/DescargaService.kt` |
| Manifiesto | `app/src/main/AndroidManifest.xml` |

## a) Manifiesto

- Permisos: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC` (obligatorio desde
  Android 14 para el tipo `dataSync`) y `POST_NOTIFICATIONS` (Android 13+).
- `<service android:name=".servicio.DescargaService" android:exported="false"
  android:foregroundServiceType="dataSync" android:process=":descarga" />`

Comprobado en el APK compilado (`aapt2 dump badging`): paquete `es.dam.psp.ut1`,
servicio `es.dam.psp.ut1.servicio.DescargaService`, tipo `dataSync` y proceso
`:descarga`.

## b) DescargaService

- `onStartCommand` llama a `startForeground` con una notificación con barra de
  progreso (canal `canal_descarga`).
- La descarga se simula en el hilo `HiloDescarga` (11 pasos de 10 % cada 500 ms) y
  actualiza la notificación en cada paso.
- `onDestroy` interrumpe el hilo: la descarga se cancela y la notificación se
  retira. Al completarse, el servicio sale de primer plano dejando visible
  «Descarga completada» y se detiene.
- El cierre de la descarga se hace en el hilo `main` (con un `Handler`) para que
  no se intercale con `onStartCommand` ni con `onDestroy`.

## c) MainActivity

Muestra «PID de la actividad: N» (`Process.myPid()`), los botones **Iniciar** y
**Parar**, y en Android 13 o superior pide `POST_NOTIFICATIONS` al abrirse.

## d) PID y nombre de hilo (sin `android:process`)

Los mensajes de Logcat usan el tag `PSP-UT1` y el formato
`evento · PID=… · hilo=…` en la actividad y en el servicio (`onCreate`,
`onStartCommand`, cada paso del bucle y `onDestroy`).

**¿Por qué los nombres de hilo son distintos?** Android llama a todos los métodos
del ciclo de vida (`onCreate`, `onStartCommand`, `onDestroy`) en el hilo
principal (`main`), el mismo que dibuja la interfaz. Si la descarga se hiciera
ahí, la app se congelaría (y Android mostraría un ANR). Por eso el bucle se
ejecuta en un hilo creado por el servicio, llamado `HiloDescarga`.

**¿Por qué el PID es el mismo que el de la actividad?** Por defecto todos los
componentes de una aplicación (actividades, servicios…) se ejecutan en **un único
proceso** del sistema, `es.dam.psp.ut1`. Un servicio **no** es un proceso ni un
hilo aparte: es un componente dentro del proceso de la app. Actividad y servicio
comparten PID; lo que cambia es el hilo.

Registro de referencia obtenido en el emulador (Medium_Phone, API 37) con un APK
temporal **sin** `android:process` (la hora es la del emulador):

```text
D/PSP-UT1 ( 3530): Actividad · onCreate · PID=3530 · hilo=main
D/PSP-UT1 ( 3530): Servicio · onCreate · PID=3530 · hilo=main
D/PSP-UT1 ( 3530): Servicio · onStartCommand · PID=3530 · hilo=main
D/PSP-UT1 ( 3530): Servicio · Descargando 0 % · PID=3530 · hilo=HiloDescarga
...
D/PSP-UT1 ( 3530): Servicio · Descargando 100 % · PID=3530 · hilo=HiloDescarga
D/PSP-UT1 ( 3530): Servicio · Descarga completada · PID=3530 · hilo=HiloDescarga
D/PSP-UT1 ( 3530): Servicio · onDestroy · PID=3530 · hilo=main
```

`ps -A | grep psp` en ese escenario: una sola línea, `es.dam.psp.ut1` (PID 3530).

## e) Servicio en otro proceso (`android:process=":descarga"`)

El nombre que empieza por `:` crea un **proceso privado** de la aplicación llamado
`es.dam.psp.ut1:descarga`. Al iniciar el servicio, Android arranca ese proceso
nuevo (con su propia JVM/ART y su propio hilo `main`) y crea el servicio allí.

- `adb shell "ps -A | grep psp"` muestra **dos** procesos: `es.dam.psp.ut1`
  (actividad) y `es.dam.psp.ut1:descarga` (servicio).
- En el log del servicio aparece ahora el **PID del proceso `:descarga`**,
  distinto del que muestra la actividad en pantalla. Los nombres de hilo no
  cambian (`main` y `HiloDescarga`), pero son hilos de otro proceso.

Registro de referencia en el emulador con la versión definitiva (descarga
iniciada y parada con «Parar» al 40 %):

```text
$ adb shell "ps -A | grep psp"
u0_a238  7471  479 ... S es.dam.psp.ut1
u0_a238  8037  479 ... S es.dam.psp.ut1:descarga

D/PSP-UT1 ( 7471): Actividad · onCreate · PID=7471 · hilo=main
D/PSP-UT1 ( 8037): Servicio · onCreate · PID=8037 · hilo=main
D/PSP-UT1 ( 8037): Servicio · onStartCommand · PID=8037 · hilo=main
D/PSP-UT1 ( 8037): Servicio · Descargando 0 % · PID=8037 · hilo=HiloDescarga
...
D/PSP-UT1 ( 8037): Servicio · Descargando 40 % · PID=8037 · hilo=HiloDescarga
D/PSP-UT1 ( 8037): Servicio · onDestroy · PID=8037 · hilo=main
D/PSP-UT1 ( 8037): Servicio · Descarga cancelada · PID=8037 · hilo=HiloDescarga
```

Al parar el servicio, el proceso `:descarga` puede seguir apareciendo un tiempo
en `ps`: Android lo deja en caché y lo elimina cuando necesita memoria.

> Estos registros se obtuvieron para verificar el comportamiento; **no sustituyen
> las capturas** que pide el enunciado (en ellas debe verse tu usuario o nombre).
> Las capturas que quedaron pendientes en la revisión se listan en [`REVISION.md`](../REVISION.md).
