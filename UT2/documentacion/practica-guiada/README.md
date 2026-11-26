# Práctica guiada con IA — La taquilla concurrente

Enunciado: `UT2_Practica_guiada_IA.pdf`. Código de partida: `Codigo_guiada.zip`
(versión Kotlin), en [`UT2/hilos`](../../hilos), paquete `es.dam.psp.ut2.guiada`.

| Fichero | Papel | ¿Se modifica? |
|---------|-------|---------------|
| `Taquilla.kt` | Contrato (`Taquilla`, `TaquillaConEspera`, `PRECIO`) | **No** |
| `TaquillaIA.kt` | Código entregado por la IA, se audita en el Reto A | **No** |
| `Verificador.kt` | Prueba de estrés de los Retos A y B | Solo la línea `fabrica` |
| `VerificadorEspera.kt` | Prueba del Reto C | Solo la línea `fabricaEspera` |
| `TaquillaSegura.kt` | Reto B | Sí |
| `TaquillaEspera.kt` | Reto C | Sí |
| `hilos/AGENTS.md` | Reglas para opencode | Solo «Reglas añadidas por el alumno» |

Ejecución: desde IntelliJ, ▶ junto al `main` de `Verificador.kt` o de
`VerificadorEspera.kt`. Desde la terminal:

```bash
cd UT2/hilos
./gradlew run -Pmain=es.dam.psp.ut2.guiada.VerificadorKt
./gradlew run -Pmain=es.dam.psp.ut2.guiada.VerificadorEsperaKt
```

Equipo de las mediciones: AMD Ryzen 7 5800H (8 núcleos / 16 hilos), Windows 11
Home, JDK 21.

---

## Reto A. ¿Tenía razón la IA?

### a) Tres ejecuciones con `TaquillaIA`

Salida de la primera ejecución:

```text
Taquilla: TaquillaIA · 30 rondas · 16 hilos · aforo 200 · tiempo 227.268800ms
  R1 todo o nada                 ROTA en 5/30 rondas
  R2 asiento vendido dos veces   ROTA en 30/30 rondas
  R3 sobreventa                  ROTA en 30/30 rondas
  R4 vendidas() no cuadra        ROTA en 7/30 rondas
  R5 recaudado() no cuadra       ROTA en 10/30 rondas
  Primer fallo · Ronda 1: entregados 230 asientos, vendidas()=230, recaudado()=5750 € (debería ser 5750 €)
VEREDICTO: INCORRECTA
```

| Regla | Ejecución 1 | Ejecución 2 | Ejecución 3 |
|-------|:-----------:|:-----------:|:-----------:|
| R1 todo o nada | 5/30 | 3/30 | 5/30 |
| R2 asiento repetido | 30/30 | 30/30 | 30/30 |
| R3 sobreventa | 30/30 | 30/30 | 30/30 |
| R4 `vendidas()` | 7/30 | 4/30 | 6/30 |
| R5 `recaudado()` | 10/30 | 10/30 | 12/30 |
| Primer fallo (ronda 1) | 230 entregados, caja 5750 € (bien) | 221 entregados, caja 5425 € (debería 5525 €) | 227 entregados, caja 5625 € (debería 5675 €) |

Se rompen las cinco reglas, pero no siempre en las mismas rondas. Una condición
de carrera solo aparece cuando dos hilos se intercalan justo dentro de la zona
peligrosa, y ese intercalado lo decide el planificador del sistema operativo
(cuántos núcleos están libres, cuándo se expulsa a cada hilo, la carga del
equipo…). Cada ejecución es un reparto distinto, así que cambian los números.
R2 y R3 salen en todas las rondas porque su ventana es muy grande (todos los
hilos recorren los mismos asientos desde el 0); R1 y R4 solo aparecen cuando
quedan los últimos asientos, que es un momento muy corto de cada ronda.

### b) Secciones críticas sin proteger

Cada pieza por separado es atómica (`containsKey`, `put`, `get`, `addAndGet`),
pero `comprar` las encadena y entre una y otra puede entrar otro hilo.

**Fallo 1 · líneas 36-37: buscar y ocupar el asiento no es atómico** (`containsKey` + `put`)

| Hilo 1 | Hilo 2 |
|--------|--------|
| `asientos.containsKey(5)` → `false` | |
| | `asientos.containsKey(5)` → `false` |
| `asientos[5] = "cliente-1"`, añade 5 | |
| | `asientos[5] = "cliente-2"` (machaca al dueño), añade 5 |
| devuelve `[5, …]` | devuelve `[5, …]` |

El asiento 5 se entrega dos veces → **R2**. Como el contador suma las dos
ventas, se entregan más de 200 asientos → **R3**.

**Fallo 2 · líneas 30 y 34-43: comprobar el aforo y vender no es atómico** (comprobar y actuar)

Situación: `contador = 197` y quedan libres el 197, el 198 y el 199.

| Hilo 1 (`n = 2`) | Hilo 2 (`n = 2`) |
|------------------|------------------|
| `contador.get() + 2 = 199 ≤ 200` → sigue | |
| | `contador.get() + 2 = 199 ≤ 200` → sigue |
| ocupa 197 y 198 | |
| | solo encuentra el 199 → `asignados = [199]` |
| `contador.addAndGet(2)` → 199 | |
| | `contador.addAndGet(2)` → 201 |
| devuelve `[197, 198]` | devuelve `[199]` |

El Hilo 2 pidió 2 y recibe 1 → **R1**. Además `addAndGet(n)` suma lo pedido y
no lo entregado: `vendidas() = 201` con 200 asientos entregados → **R4**, y la
caja cobra 2 entradas por 1 asiento → **R5**. Si el Hilo 2 no encuentra
ninguno devuelve `[]` (no rompe R1) pero suma igual 2 al contador y 50 € a la caja.

**Fallo 3 · línea 44: `caja += n * PRECIO` sobre un `Long` normal**

Es leer, sumar y escribir en tres pasos:

| Hilo 1 (`n = 2`) | Hilo 2 (`n = 3`) |
|------------------|------------------|
| lee `caja` = 1000 | |
| | lee `caja` = 1000 |
| escribe 1000 + 50 = 1050 | |
| | escribe 1000 + 75 = 1075 |

Se pierde la venta del Hilo 1: 5 entradas y solo 75 € → **R5**. Es lo que pasa en
la ejecución 2 (221 entregados, faltan 100 €).

**Fallo 4 · líneas 26 y 50: visibilidad de `caja`**

`caja` no es `@Volatile` ni se lee con cerrojo, así que `recaudado()` llamado
desde otro hilo puede ver un valor antiguo. El verificador no lo detecta porque
lee la caja después de `join()`, que sí garantiza ver lo escrito por los hilos.

### c) Auditoría con la IA (agente Plan)

Prompt usado (el de ejemplo del enunciado):

> Analiza guiada/TaquillaIA.kt frente al contrato de guiada/Taquilla.kt. Enumera
> cada condición de carrera: líneas implicadas, intercalado de dos hilos que la
> produce y qué regla del contrato rompe. No modifiques ningún fichero.

| Fallo | Lo encontré yo | Lo encontró la IA | ¿Real? |
|-------|:--------------:|:-----------------:|--------|
| 1. `containsKey` + `put` (36-37) | Sí | Sí | Real (R2, y por él R3) |
| 2. Comprobar aforo y vender por separado (30-43) | Sí | Sí | Real (R1, R4, R5) |
| 3. `caja +=` sin sincronizar (44) | Sí | Sí | Real (R5) |
| 4. `addAndGet(n)` suma lo pedido, no lo entregado (43) | Sí (dentro del 2) | Sí, como fallo aparte | Real, aunque solo se nota cuando ocurre el 2 |
| 5. Visibilidad de `caja` en `recaudado()` (50) | Sí | Sí | Real, pero el verificador no lo ve |
| 6. «`vendidas()` puede devolver un valor desactualizado» | No | Sí | Falso positivo: `AtomicInteger` ya garantiza visibilidad; que el valor cambie justo después es normal |

### d) ¿Por qué «es thread-safe porque usa AtomicInteger y ConcurrentHashMap» es falso?

Esas clases solo hacen atómica **cada operación suelta**. `comprar` es una
secuencia (comprobar el aforo, buscar asientos, ocuparlos, sumar contador y
caja) que debe verse como un único paso, y además mantiene un invariante entre
tres datos (asientos, contador y caja) que ninguna de las dos clases conoce. Y
`caja` ni siquiera es atómica.

Relación entre reglas rotas y fallos:

| Regla | Fallo que la causa | ¿Consecuencia de otro fallo? |
|-------|--------------------|------------------------------|
| R1 | Fallo 2 | — |
| R2 | Fallo 1 | — |
| R3 | Fallo 1 | **Sí.** Los números de asiento solo van de 0 a 199, así que la única forma de entregar más de 200 es repetir asientos: R3 se rompe porque se rompe R2 (por eso las dos salen 30/30) |
| R4 | Fallo 2 (+4) | **Sí**, del fallo 2: se suma `n` aunque se hayan entregado menos |
| R5 | Fallo 3 y fallo 2 | En parte: la venta parcial cobra de más; la pérdida de dinero es del fallo 3. En la ejecución 1 se rompen R2 y R3 en la ronda 1 con la caja bien, así que R5 tiene causa propia |

---

## Reto B. Que la IA lo arregle… y demostrar que está bien

### a) Prompt (agente Build)

> Implementa la clase TaquillaSegura en guiada/TaquillaSegura.kt cumpliendo el
> contrato de guiada/Taquilla.kt. comprar(cliente, n) es todo o nada: si no
> quedan n asientos libres no vende ninguno y devuelve una lista vacía; si
> quedan, los vende todos a ese cliente, suma n a vendidas y n × PRECIO a la caja.
>
> En TaquillaIA encontré estos fallos y no deben repetirse: (1) containsKey y
> put sobre el mapa no son atómicos y dos hilos se llevan el mismo asiento;
> (2) se comprueba el aforo con contador.get() y se vende después, así que dos
> hilos pasan la comprobación y uno recibe menos asientos de los pedidos;
> (3) contador.addAndGet(n) suma lo pedido aunque se entreguen menos; (4) caja
> += n * PRECIO no es atómico y se pierden ventas; (5) recaudado() lee caja sin
> sincronizar.
>
> Usa un solo cerrojo con synchronized sobre un objeto privado que proteja los
> tres datos (dueño de cada asiento, vendidas y caja). La comprobación de que
> quedan n libres y la venta tienen que estar dentro del mismo bloque
> synchronized. vendidas() y recaudado() también deben leer con el cerrojo. No
> uses AtomicInteger ni ConcurrentHashMap dentro del synchronized. Modifica solo
> TaquillaSegura.kt y explica qué invariante protege el cerrojo.

### b) Revisión del diff antes de aceptar

En la ventana Commit solo aparece `TaquillaSegura.kt`: no toca ningún fichero
prohibido. Usa `synchronized` sobre un `Any()` privado, un `arrayOfNulls<String>`
para los dueños y dos variables normales: todo está en el apartado 6.1 de los
apuntes. Nada que deshacer.

### c) Verificador con `fabrica = ::TaquillaSegura`

Vueltas necesarias: **1** (la primera versión pasó las tres ejecuciones).

```text
Taquilla: TaquillaSegura · 30 rondas · 16 hilos · aforo 200 · tiempo 163.070400ms
  R1 todo o nada                 OK
  R2 asiento vendido dos veces   OK
  R3 sobreventa                  OK
  R4 vendidas() no cuadra        OK
  R5 recaudado() no cuadra       OK
VEREDICTO: ninguna regla rota en 30 rondas (no demuestra que sea correcta)
```

Las ejecuciones 2 y 3 dieron lo mismo (todo OK) en 155,6 ms y 193,6 ms.

### d) Lista de comprobación

| Pregunta | Respuesta para `TaquillaSegura` |
|----------|---------------------------------|
| ¿Sección crítica y cerrojo? ¿Todo el estado con el mismo cerrojo? | La sección crítica es todo el cuerpo de `comprar` desde que mira cuántos quedan hasta que actualiza la caja. La protege `synchronized(cerrojo)`, y `duenos`, `vendidas` y `caja` solo se tocan dentro de ese mismo cerrojo. Invariante: `vendidas` = asientos con dueño y `caja` = `vendidas` × 25 |
| ¿Queda algún «comprobar y actuar» fuera del cerrojo? | No. El `if (aforo - vendidas < n)` y la búsqueda con `if (duenos[asiento] == null)` están dentro del bloque. Lo único fuera es `if (n <= 0)`, que no mira estado compartido |
| ¿Los métodos de lectura usan cerrojo, `@Volatile` o atómica? | Sí, `vendidas()` y `recaudado()` hacen `synchronized(cerrojo)`. Sin eso, otro hilo podría ver un valor antiguo (visibilidad) o, con `Long` en algunas JVM, un valor a medio escribir |
| ¿Mezcla herramientas sin necesidad? | No: dentro del `synchronized` solo hay un array y variables normales. Meter `AtomicInteger` ahí no aportaría nada |
| ¿Más de un cerrojo? ¿Mismo orden? | Un único cerrojo, así que no puede haber interbloqueo por orden |

### e) ¿Por qué «no demuestra que sea correcta»?

Porque una prueba solo enseña los intercalados que han salido en esas 30
rondas; que no aparezca un fallo no quiere decir que no pueda aparecer en otra
máquina o con otra carga. Además el verificador no mira todo. Ejemplo que la
lista detecta y el verificador no: si `vendidas()` y `recaudado()` leyeran sin
cerrojo (`= caja` a secas), el verificador seguiría saliendo OK, porque solo los
llama desde `main` después de `join()`, y `join()` ya garantiza ver lo escrito
por los hilos. El problema de visibilidad solo aparece si se consulta la caja
mientras se está vendiendo, y eso el verificador no lo hace. Tampoco detectaría,
por ejemplo, dos cerrojos tomados en distinto orden si en la prueba no coinciden.

Tiempos (mismo equipo, tres ejecuciones de 30 rondas):

| Taquilla | Ejec. 1 | Ejec. 2 | Ejec. 3 | Media |
|----------|--------:|--------:|--------:|------:|
| `TaquillaIA` | 227,3 ms | 244,4 ms | 205,9 ms | 225,9 ms |
| `TaquillaSegura` | 163,1 ms | 155,6 ms | 193,6 ms | 170,8 ms |

La corrección no ha costado nada apreciable; de hecho la segura ha salido algo
más rápida. El tiempo lo domina crear y arrancar 16 hilos × 30 rondas, y cada
compra dentro del cerrojo es muy corta. `TaquillaIA` no gana nada por no
bloquear: sus hilos compiten igual sobre el `ConcurrentHashMap` y además siguen
vendiendo asientos repetidos que ya no existen. Con tan pocos datos y tan poco
tiempo la diferencia está dentro del ruido; lo importante es que la versión
correcta no es más lenta de forma apreciable.
