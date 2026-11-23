# Práctica 4 — Coordinador de procesos en paralelo

Código (paquete `es.dam.psp.ut1.practicas`):

| Fichero | Papel | ¿Se modifica? |
|---------|-------|---------------|
| `GeneradorDatos.kt` | Crea `datos/` con ocho ficheros (~32 MB) | **No** |
| `Contador.kt` | Hijo: lee stdin y escribe `lineas;palabras;caracteres;palabraMasFrecuente` | **No** |
| `Coordinador.kt` | Padre: reparte los ficheros entre hijos | Sí (única versión) |

> **Bloqueo pendiente:** `GeneradorDatos.kt` y `Contador.kt` del repositorio
> **no son los originales del profesor** (el generador usa `Random.Default` sin
> semilla, así que no puede generar «exactamente los mismos ficheros» que la
> versión Java). Los originales (carpeta `Codigo_practicas`) no están ni en el
> repositorio ni en el historial de Git ni en este equipo, así que **no se han
> inventado**. Se han conservado byte a byte y solo se han movido a su paquete.
> Hay que copiar encima los originales, regenerar `datos/` y repetir las
> mediciones (ver `PENDIENTES_EVIDENCIAS.md`).

```bash
cd procesos
./gradlew run -Pmain=es.dam.psp.ut1.practicas.GeneradorDatosKt
./gradlew run -Pmain=es.dam.psp.ut1.practicas.CoordinadorKt
./gradlew run -Pmain=es.dam.psp.ut1.practicas.CoordinadorKt --args="limitado"   # mejora opcional
```

## Funciones del Coordinador

| Apartado | Función | Qué hace |
|----------|---------|----------|
| a | `lanzarContador(fichero)` | `java -cp <classpath> es.dam.psp.ut1.practicas.ContadorKt` con `redirectInput(fichero)` y `redirectError(INHERIT)` |
| b | `recogerResultado(fichero, hijo)` | Lee la línea, `waitFor()`; si el código no es 0 lanza `IOException` con el fichero y el código |
| c | `secuencial(ficheros)` | Lanza un hijo y recoge su resultado antes de lanzar el siguiente |
| d | `concurrente(ficheros)` | Lanza los ocho hijos y después recoge los resultados en orden |
| e | `main` | Tabla por fichero con fila `TOTAL` y comparación de ambos modos (igualdad de `Resultado`, que es `data class`) |
| opcional | `concurrenteLimitado(ficheros, maximo)` | Nunca más de `maximo` hijos vivos: al recoger uno se lanza el siguiente |

No hay riesgo de interbloqueo por tuberías: cada hijo escribe una sola línea en
stdout y stderr se hereda.

## f) Mediciones

**Equipo y condiciones:** AMD Ryzen 7 5800H (8 núcleos físicos, 16 procesadores
lógicos), 13,9 GB de RAM, SSD NVMe, Windows 11 Home, JDK 21. Portátil **con
batería** y plan de energía «Equilibrado». Ejecución con `java -cp` desde la
terminal, sin el IDE abierto, el 23/11/2026. Datos: los ocho ficheros actuales de
`datos/` (33 554 676 caracteres en total).

| Prueba | Secuencial (ms) | Concurrente (ms) | Aceleración |
|-------:|----------------:|-----------------:|------------:|
| 1 | 4329 | 1330 | 3,25 |
| 2 | 4268 | 1440 | 2,96 |
| 3 | 4291 | 1285 | 3,34 |
| **Media** | **4296** | **1352** | **3,18** |

En las tres pruebas los dos modos dieron exactamente el mismo resultado
(TOTAL: 511 515 líneas, 4 092 120 palabras, 33 554 676 caracteres).

Mejora opcional (`limitado`, misma sesión, secuencial de referencia 4590 ms):

| Máximo de hijos | 1 | 2 | 4 | 8 |
|-----------------|---|---|---|---|
| Tiempo (ms) | 4494 | 2538 | 1607 | 1353 |
| Aceleración | 1,02 | 1,81 | 2,86 | 3,39 |

**Coste de arrancar las JVM** (mismo Coordinador sobre 8 ficheros *vacíos*, es
decir, solo arrancar procesos): secuencial ≈ **2010 ms** (≈ 250 ms por JVM) y
concurrente ≈ **634 ms** (media de 3 ejecuciones: 2087/625, 1995/667, 1948/611).

> Las tablas del documento de entrega (3600/1079 ms, etc.) se obtuvieron con
> otra versión del código; sustitúyelas por estas o por las que obtengas tras
> restaurar los originales.

## ¿Por qué la aceleración (≈ 3,2) no llega al número de núcleos (16)?

1. **El techo real no es 16.** Solo hay **8 ficheros**, luego como mucho hay 8
   hijos trabajando a la vez. Además, los 16 procesadores lógicos son 8 núcleos
   físicos con SMT: dos hilos del mismo núcleo comparten sus unidades de
   cálculo. El máximo razonable es ≈ 8.

2. **Ley de Amdahl: hay partes que siguen siendo secuenciales.** Si una fracción
   *s* del trabajo no se puede repartir, la aceleración máxima con *n*
   procesadores es `1 / (s + (1 − s)/n)`. Aquí el padre lanza los hijos uno a
   uno (`start()` es secuencial), recoge los resultados uno a uno, compara e
   imprime. Con la aceleración medida (3,18) y n = 8, despejando sale
   s ≈ 0,22: un 22 % del tiempo se comporta como secuencial, y con eso el límite
   aunque hubiera infinitos núcleos sería ≈ 1/0,22 ≈ 4,6.

3. **Arrancar una JVM es caro.** Cada hijo es una JVM nueva (cargar el JDK, las
   clases de Kotlin, crear sus hilos de GC y JIT). Medido: arrancar 8 JVM vacías
   cuesta ≈ 2010 ms en secuencial, casi la mitad de los 4296 ms totales. Ese
   coste no es trabajo útil y, en paralelo, tampoco baja a 2010/8 ≈ 250 ms sino a
   ≈ 634 ms, porque los ocho arranques compiten entre sí.

4. **Contención de CPU y memoria.** Cada JVM crea varios hilos de GC y de
   compilación pensados para 16 CPU: ocho JVM a la vez tienen muchos más hilos
   que núcleos. `Contador` carga cada fichero entero en memoria y crea ~511 000
   cadenas, así que los ocho procesos compiten por el ancho de banda de memoria y
   la caché L3 compartida. La E/S de disco pesa poco aquí porque, tras la primera
   lectura, los 32 MB quedan en la caché del sistema operativo.

5. **Frecuencia del procesador.** Con un solo núcleo activo el Ryzen sube más de
   frecuencia que con todos activos, y el equipo estaba con batería en modo
   «Equilibrado»: cada núcleo va más lento en el modo concurrente.

La tabla de `concurrenteLimitado` lo confirma: de 1 a 2 hijos casi se duplica
la velocidad (1,81), pero de 4 a 8 solo pasa de 2,86 a 3,39.
