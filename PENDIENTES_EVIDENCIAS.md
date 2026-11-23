# Pendientes y evidencias para la nueva entrega

El documento de entrega (`documentacion/UT-1_entrega_v1.pdf`) es una exportación
a PDF y no hay versión editable en el repositorio. Esta lista indica qué cambiar
en el documento original (Notion/Word) y qué capturas faltan.

**Regla para todas las capturas:** debe verse tu usuario o tu nombre (por ejemplo,
la ruta `C:\Users\DIEGO\...` en la consola de IntelliJ, el prompt `PS C:\Users\DIEGO>`
o tu nombre en la barra del sistema). No uses los registros de texto del
repositorio como si fueran capturas.

## 0. Bloqueo: originales de GeneradorDatos y Contador

- [ ] Copiar los originales del profesor (`Codigo_practicas/procesos/.../practicas/GeneradorDatos.kt`
      y `Contador.kt`) encima de
      `procesos/src/main/kotlin/es/dam/psp/ut1/practicas/`. No están en este
      equipo, ni en el repositorio, ni en su historial; **no se han inventado**.
- [ ] Comprobar que el nombre de la clase sigue siendo `ContadorKt` (el
      Coordinador lanza `es.dam.psp.ut1.practicas.ContadorKt`) y compilar.
- [ ] Borrar `procesos/datos/` y regenerar con `GeneradorDatosKt`.
- [ ] Repetir las tres mediciones de la práctica 4 y actualizar la tabla.
- [ ] Hacer un commit: `fix(practica4): restaurar generador y contador originales`.

## Práctica 1 — documento

- [ ] Tabla resumen del apartado b): la columna «Retorno medio» contiene el
      tiempo de respuesta. Valores correctos: FCFS 12,2 · SJF 10,4 · RR 12,6 ·
      Prioridades no apropiativo 11,2 (detalle en `documentacion/practica1/README.md`).
- [ ] «¿Cuál da la menor espera media?»: de los cuatro pedidos, **SJF (5,8)**.
- [ ] (Revisable) FCFS perjudica más a P5 (espera 14) o a P4 (retorno/ráfaga 5,3), no a P2.

## Práctica 2 — capturas y documento

- [ ] a) Añadir una tabla con los cinco procesos, su PID y si son aplicación de
      usuario o servicio del sistema.
- [ ] b) Completar el árbol: Dormilon → padre → … → `idea64.exe`, con PID, PPID y
      nombre de cada eslabón, y dibujarlo. Herramienta: Process Explorer o
      `Get-CimInstance Win32_Process -Filter "ProcessId=<pid>"` repetido hacia arriba.
- [ ] c) (Opcional) Nueva captura de `BuscarProcesosKt java` (la clase se llamaba
      `BuscarProcesoKt`).

## Práctica 3 — documento y captura

- [ ] **Corregir el texto del timeout**: no «devuelve el código 1», sino que el
      Lanzador termina el proceso y sus descendientes, avisa y usa **-1**. Texto
      propuesto en `documentacion/practica3/README.md`.
- [ ] Nueva captura de una sesión con la versión definitiva:
      `echo Hola`, `type archivo_inexistente.txt`, `comando_que_no_existe`,
      `ping -n 11 127.0.0.1`, `salir`. Deben verse `[código 0 …]`, `[código 1 …]`,
      `[código 1 …]` y `Tiempo límite … [código -1 …]`.
- [ ] Captura de `procesos/historial.txt` con la línea `ping -n 11 127.0.0.1;-1;…`.
      (Tu historial anterior sigue en local; bórralo antes si quieres uno limpio.)

## Práctica 4 — documento y capturas

- [ ] Sustituir la captura de la pág. 9 que muestra `ClassNotFoundException`
      (era de una versión rota).
- [ ] Captura de una ejecución de `CoordinadorKt` con la tabla, la fila TOTAL,
      «CORRECTO: ambos modos…» y los tiempos.
- [ ] Tabla de tres ejecuciones: usar la de `documentacion/practica4/README.md`
      o, mejor, repetirla tras el punto 0.
- [ ] **Añadir la explicación de por qué la aceleración no llega al número de
      núcleos** (texto en `documentacion/practica4/README.md`: 8 ficheros y
      8 núcleos físicos, Amdahl, coste de arrancar JVM, contención y frecuencia).
- [ ] (Opcional) Captura de `CoordinadorKt limitado` para la mejora opcional.

## Práctica 5 — documento y capturas (en el emulador o un móvil)

Preparación: abrir `ServicioPSP/` en Android Studio, sincronizar Gradle y
desinstalar la app antigua `com.example.ut1` si sigue en el emulador.

- [ ] Sustituir la captura del manifiesto (mostraba `.SyncService`) por la del
      manifiesto actual (`.servicio.DescargaService`, `dataSync`, `:descarga`).
- [ ] Sustituir la captura de la pág. 14 (era un programa de consola, no el
      servicio Android) por una de `DescargaService.kt` o de la app funcionando.
- [ ] Captura de la actividad con «PID de la actividad: N» e Iniciar / Parar.

**Apartado d) — sin proceso separado.** Quitar temporalmente la línea
`android:process=":descarga"` del manifiesto, ejecutar y pulsar Iniciar:

- [ ] Captura de la **notificación con la barra de progreso** (desplegar la
      barra de notificaciones durante los ~5 s de descarga).
- [ ] Captura de **Logcat con el filtro `tag:PSP-UT1`** donde se vea el mismo PID
      en `Actividad · onCreate`, `Servicio · onCreate` (hilo `main`) y
      `Servicio · Descargando …` (hilo `HiloDescarga`).
- [ ] Responder: hilos distintos y mismo PID (texto en `documentacion/practica5/README.md`).

**Apartado e) — con proceso separado.** Volver a poner
`android:process=":descarga"`, ejecutar y pulsar Iniciar:

- [ ] Con la descarga en marcha, captura de la terminal de Android Studio con
      `adb shell "ps -A | grep psp"` mostrando `es.dam.psp.ut1` y
      `es.dam.psp.ut1:descarga` (con el paquete antiguo `com.example.ut1` el
      `grep psp` no encontraba nada).
- [ ] Captura de Logcat `tag:PSP-UT1` donde el PID del servicio sea distinto del
      de la actividad.
- [ ] Explicar qué ha cambiado (texto en `documentacion/practica5/README.md`).

## Repositorio

- [ ] Revisar los commits locales y hacer `git push` cuando lo autorices
      (no se ha hecho push).
- [ ] Añadir el enlace al repositorio en el documento de entrega.
