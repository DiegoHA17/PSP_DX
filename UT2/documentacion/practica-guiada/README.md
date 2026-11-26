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
Home, JDK 21. Las salidas de este documento son de ejecuciones reales desde la
terminal (`java` sobre las clases compiladas por Gradle), no desde IntelliJ.

## Quién ha hecho qué

| Parte | Hecho por |
|-------|-----------|
| Análisis de `TaquillaIA`, intercalados y tablas de reglas (Reto A a, b y d) | Claude (asistente de programación) |
| Código de `TaquillaSegura` y `TaquillaEspera` | Claude, escrito directamente (no con el agente Build de opencode) |
| Ejecuciones de los verificadores, la versión rota con `if` y la prueba con `wait(50)` | Claude, en la terminal |
| Auditoría de `TaquillaIA` (Reto A c) y pregunta sobre `notify()` (Reto C e) | Codex, en modo solo lectura (respuestas reales) |
| Revisión independiente del código y de este documento | Codex, en modo solo lectura |
| Uso de opencode (Plan y Build), revisión del diff en IntelliJ, capturas con el usuario y reflexión final | **Pendiente del alumno**: ver [`PENDIENTES.md`](PENDIENTES.md) |

Todavía no se ha usado opencode. Los prompts para opencode que aparecen abajo
están preparados, pero no se han enviado.

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
(núcleos libres, cuándo se expulsa a cada hilo, carga del equipo…). Cada
ejecución es un reparto distinto, así que cambian los números. R2 y R3 salen en
todas las rondas porque todos los hilos recorren los mismos asientos desde el 0
a la vez; R1 y R4 solo aparecen cuando quedan los últimos asientos, que es un
momento muy corto de cada ronda.

### b) Secciones críticas sin proteger

Cada pieza por separado es atómica (`containsKey`, `put`, `get`, `addAndGet`),
pero `comprar` las encadena y entre una y otra puede entrar otro hilo.

**Fallo 1 · líneas 36-37: buscar y ocupar el asiento no es atómico** (`containsKey` + `put`)

| Paso | Hilo 1 | Hilo 2 |
|:----:|--------|--------|
| 1 | `asientos.containsKey(5)` → `false` | |
| 2 | | `asientos.containsKey(5)` → `false` |
| 3 | `asientos[5] = "cliente-1"`, añade 5 | |
| 4 | | `asientos[5] = "cliente-2"` (machaca al dueño), añade 5 |
| 5 | devuelve `[5, …]` | devuelve `[5, …]` |

El asiento 5 se entrega dos veces → **R2**. Esto por sí solo no es sobreventa:
el contador cuenta también los repetidos, así que llegaría a 200 y la
comprobación de la línea 30 frenaría las ventas. Para entregar más de 200 hace
falta que, además, varios hilos pasen esa comprobación a la vez (fallo 2). Con
16 hilos comprando a la vez eso pasa constantemente, y por eso R3 sale en todas
las rondas → **R3** necesita el fallo 1 y el fallo 2 juntos.

**Fallo 2 · líneas 30 y 34-43: comprobar el aforo y vender no es atómico, y se suma lo pedido**

Situación: `contador = 197` y quedan libres el 197, el 198 y el 199.

| Paso | Hilo 1 (`n = 2`) | Hilo 2 (`n = 2`) |
|:----:|------------------|------------------|
| 1 | `contador.get() + 2 = 199 ≤ 200` → sigue | |
| 2 | | `contador.get() + 2 = 199 ≤ 200` → sigue |
| 3 | ocupa 197 y 198 | |
| 4 | | solo encuentra el 199 → `asignados = [199]` |
| 5 | `contador.addAndGet(2)` → 199 | |
| 6 | | `contador.addAndGet(2)` → 201 |
| 7 | devuelve `[197, 198]` | devuelve `[199]` |

El Hilo 2 pidió 2 y recibe 1 → **R1**. Además `addAndGet(n)` (línea 43) suma lo
pedido y no lo entregado: `vendidas() = 201` con 200 asientos entregados → **R4**,
y la caja cobra 2 entradas por 1 asiento → **R5**. Si el Hilo 2 no encuentra
ninguno devuelve `[]` (no rompe R1) pero suma igual 2 al contador y 50 € a la caja.

**Fallo 3 · línea 44: `caja += n * PRECIO` sobre un `Long` normal**

Es leer, sumar y escribir en tres pasos:

| Paso | Hilo 1 (`n = 2`) | Hilo 2 (`n = 3`) |
|:----:|------------------|------------------|
| 1 | lee `caja` = 1000 | |
| 2 | | lee `caja` = 1000 |
| 3 | escribe 1000 + 50 = 1050 | |
| 4 | | escribe 1000 + 75 = 1075 |

Se pierde la venta del Hilo 1: 5 entradas y solo 75 € → **R5**. En las
ejecuciones 2 y 3 falta dinero en la caja (100 € y 50 €), que solo puede pasar
por pérdidas como esta: la venta parcial del fallo 2 cobra de más, no de menos.

**Fallo 4 · líneas 26 y 50: visibilidad de `caja`**

`caja` no es `@Volatile` ni se lee con cerrojo, así que un hilo puede no ver lo
que ha escrito otro:

| Paso | Hilo 1 | Hilo 2 |
|:----:|--------|--------|
| 1 | | `recaudado()` → lee `caja` = 0 |
| 2 | compra 1 entrada: `caja = 25` | |
| 3 | | `recaudado()` → puede seguir leyendo 0 (valor guardado en caché) |

El verificador no lo detecta porque lee la caja después de `join()`, y `join()`
sí garantiza ver lo que escribieron los hilos.

### c) Auditoría con IA

> **Pendiente:** el enunciado pide hacerla con opencode (agente Plan). Por ahora
> se ha hecho con **Codex** en modo solo lectura y con el mismo prompt.

Prompt (el de ejemplo del enunciado):

> Analiza guiada/TaquillaIA.kt frente al contrato de guiada/Taquilla.kt. Enumera
> cada condición de carrera: líneas implicadas, intercalado de dos hilos que la
> produce y qué regla del contrato rompe. No modifiques ningún fichero.

Codex devolvió seis carreras. La primera vez se quedó colgado a mitad de la
respuesta y hubo que repetir la pregunta pidiendo una respuesta más corta.

| Fallo | Análisis del apartado b | Lo encontró Codex | ¿Real? |
|-------|:-----------------------:|:-----------------:|--------|
| `containsKey` + `put` (36-37) | Sí (fallo 1) | Sí (su n.º 2) | Real: R2. Dice que también rompe R4 porque el mapa guarda un dueño y el contador suma dos. Según el contrato tiene sentido, pero el verificador cuenta los asientos entregados (dos), así que R4 no sale por esto |
| Comprobar el aforo con `contador.get()` y vender después (30, 43) | Sí (fallo 2) | Sí (su n.º 1) | Real: R3. Su intercalado (aforo 1, los dos pasan la comprobación y los dos ven libre el asiento 0) junta este fallo con el 1, y lo dice («además de R2 por la carrera siguiente»). Es correcto: R3 necesita los dos |
| Compra parcial: `addAndGet(n)` y cobro de lo pedido (30-45) | Sí (fallo 2) | Sí (su n.º 3) | Real: R1, R4 y R5. Su intercalado es correcto |
| `caja +=` sin sincronizar (44) | Sí (fallo 3) | Sí (su n.º 4) | Real: R5 |
| `vendidas()` y `recaudado()` se leen por separado (43-44, 48, 50) | No | Sí (su n.º 5) | **Falso positivo para el contrato.** Entre las dos líneas el contador ya ha subido y la caja todavía no, pero el contrato no pide leer las dos cosas a la vez y R5 se comprueba al final. En `TaquillaSegura` también son dos llamadas separadas |
| Visibilidad de `caja` en `recaudado()` (26, 50) | Sí (fallo 4) | Sí (su n.º 6) | Real, pero el verificador no lo ve (lee después de `join()`) |

Codex no se dejó ninguno de los fallos del apartado b. Su único punto flojo es
el n.º 5, que no rompe ninguna regla del contrato. En la primera versión de esta
tabla se marcó su n.º 1 como «mal atribuido», y era un error nuestro: en la
revisión final el propio Codex hizo ver que R3 necesita también la carrera de la
comprobación del aforo. Es un buen ejemplo de por qué hay que comprobar también
las correcciones que se le hacen a la IA.

### d) ¿Por qué «es thread-safe porque usa AtomicInteger y ConcurrentHashMap» es falso?

Esas clases solo hacen atómica **cada operación suelta**. `comprar` es una
secuencia (comprobar el aforo, buscar asientos, ocuparlos, sumar contador y
caja) que tendría que hacerse de una vez, y además tiene que mantener cuadrados
tres datos (asientos, contador y caja), cosa que ninguna de esas clases sabe
hacer. Y `caja` ni siquiera es atómica.

| Regla | Fallo que la causa | ¿Consecuencia de otro fallo? |
|-------|--------------------|------------------------------|
| R1 | Fallo 2 | — |
| R2 | Fallo 1 | — |
| R3 | Fallos 1 y 2 juntos | **Sí.** Los asientos solo van de 0 a 199, así que la única forma de entregar más de 200 es repetir asientos: si R3 se rompe, R2 también. Además, hace falta que varios hilos pasen a la vez la comprobación del aforo, porque si no el contador frenaría las ventas al llegar a 200 |
| R4 | Fallo 2 | **Sí**: es efecto de la compra parcial (se suma `n` aunque se entreguen menos) |
| R5 | Fallo 3 y fallo 2 | En parte: la venta parcial cobra de más; la pérdida de dinero es del fallo 3. En la ejecución 1 la ronda 1 rompe R2 y R3 con la caja bien, así que R5 no depende de R2 |

---

## Reto B. Que la IA lo arregle… y demostrar que está bien

### a) Prompt para opencode (agente Build)

> **Pendiente de enviar.** `TaquillaSegura` la escribió Claude directamente. Este
> es el prompt preparado para el Reto B; hay que enviarlo en opencode y anotar
> qué pasa (ver `PENDIENTES.md`).

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

### b) Qué cambia el código

Respecto al código de partida, en la rama solo cambian `TaquillaSegura.kt` y la
línea `fabrica` de `Verificador.kt` (comprobado con `diff` contra el zip). Usa
`synchronized` sobre un `Any()` privado, un `arrayOfNulls<String>` para los
dueños y dos variables normales: todo es del apartado 6.1 de los apuntes.

> **Pendiente:** revisar el diff de lo que genere opencode en la ventana Commit
> de IntelliJ (ficheros tocados y herramientas usadas).

### c) Verificador con `fabrica = ::TaquillaSegura`

```text
Taquilla: TaquillaSegura · 30 rondas · 16 hilos · aforo 200 · tiempo 163.070400ms
  R1 todo o nada                 OK
  R2 asiento vendido dos veces   OK
  R3 sobreventa                  OK
  R4 vendidas() no cuadra        OK
  R5 recaudado() no cuadra       OK
VEREDICTO: ninguna regla rota en 30 rondas (no demuestra que sea correcta)
```

Las ejecuciones 2 y 3 dieron lo mismo (todo OK) en 155,6 ms y 193,6 ms. La
versión de Claude pasó a la primera.

> **Pendiente:** número de vueltas con opencode y captura final.

### d) Lista de comprobación

| Pregunta | Respuesta para `TaquillaSegura` |
|----------|---------------------------------|
| ¿Sección crítica y cerrojo? ¿Todo el estado con el mismo cerrojo? | La sección crítica es `comprar` desde que mira cuántos quedan hasta que actualiza la caja. La protege `synchronized(cerrojo)`, y `duenos`, `vendidas` y `caja` solo se tocan dentro de ese cerrojo. Así siempre se cumple: `vendidas` = asientos con dueño y `caja` = `vendidas` × 25 |
| ¿Queda algún «comprobar y actuar» fuera del cerrojo? | No. El `if (aforo - vendidas < n)` y el `if (duenos[asiento] == null)` están dentro. Lo único fuera es `if (n <= 0)`, que no mira datos compartidos |
| ¿Los métodos de lectura usan cerrojo, `@Volatile` o atómica? | Sí, `vendidas()` y `recaudado()` usan `synchronized(cerrojo)`. Sin eso, otro hilo podría ver un valor antiguo (problema de visibilidad) |
| ¿Mezcla herramientas sin necesidad? | No: dentro del `synchronized` solo hay un array y variables normales |
| ¿Más de un cerrojo? ¿Mismo orden? | Un único cerrojo, así que no hay riesgo de interbloqueo por orden |

### e) ¿Por qué «no demuestra que sea correcta»?

Una prueba solo enseña los intercalados que han salido en esas 30 rondas; que
no aparezca un fallo no quiere decir que no pueda aparecer en otra máquina o con
otra carga. Además, el verificador no lo mira todo. Algo que la lista detecta y
el verificador no: si `vendidas()` y `recaudado()` leyeran sin cerrojo, el
verificador seguiría saliendo OK, porque solo las llama desde `main` después de
`join()`. El fallo de visibilidad solo aparece si se consulta la caja mientras
se vende, y el verificador no lo hace.

Tiempos (mismo equipo, tres ejecuciones de 30 rondas):

| Taquilla | Ejec. 1 | Ejec. 2 | Ejec. 3 | Media |
|----------|--------:|--------:|--------:|------:|
| `TaquillaIA` | 227,3 ms | 244,4 ms | 205,9 ms | 225,9 ms |
| `TaquillaSegura` | 163,1 ms | 155,6 ms | 193,6 ms | 170,8 ms |

La corrección no ha salido más cara; en estas mediciones la segura ha tardado
incluso algo menos. No se puede sacar mucho más: son solo tres ejecuciones, los
tiempos varían bastante entre ellas y las dos clases no hacen el mismo trabajo
(la de la IA reparte asientos repetidos). Cada compra dentro del cerrojo es muy
corta, así que esperar al cerrojo apenas se nota.

---

## Reto C. Devoluciones y lista de espera

### a) Regla añadida a `hilos/AGENTS.md`

> Cuando un hilo tenga que esperar a que cambie el estado (por ejemplo, a que haya
> un asiento libre), usa `wait()` sin tiempo dentro de `synchronized` sobre el
> mismo cerrojo que protege ese estado, y siempre dentro de un `while` que vuelva
> a comprobar la condición al despertar. No uses `Thread.sleep`,
> `wait(milisegundos)` ni bucles que miren el estado una y otra vez: el hilo debe
> quedar en estado WAITING. Quien cambie el estado (devolver un asiento) avisa con
> `notifyAll()`. Si interrumpen al hilo mientras espera, captura
> `InterruptedException` y devuelve `null`.

### b) Implementación

> **Pendiente de enviar.** `TaquillaEspera` la escribió Claude directamente. Prompt
> preparado para opencode (agente Build):

> Implementa TaquillaEspera en guiada/TaquillaEspera.kt cumpliendo
> TaquillaConEspera de guiada/Taquilla.kt. comprar, vendidas y recaudado igual
> que en TaquillaSegura (un solo cerrojo para dueños, vendidas y caja).
> devolver(cliente, asiento) solo funciona si el asiento es de ese cliente: lo
> libera, resta 1 a vendidas, devuelve 25 € de la caja y avisa a los que esperan.
> esperarEntrada(cliente) espera mientras no haya asientos libres y luego vende
> uno. Sigue las reglas de AGENTS.md sobre cómo esperar. Modifica solo
> TaquillaEspera.kt.

Cómo es el código: el cerrojo es un `Object()` (con `Any()` Kotlin no deja
llamar a `wait()`/`notifyAll()`), la venta de un asiento está en una función
privada `vender` que solo se llama con el cerrojo cogido, `devolver` comprueba
el rango y el dueño antes de cambiar nada, y `esperarEntrada` hace
`while (vendidas == aforo) cerrojo.wait()`. Al capturar `InterruptedException`
vuelve a marcar el hilo como interrumpido antes de devolver `null`, para no
perder la interrupción.

### c) `VerificadorEspera`

Versión final (tres ejecuciones, todas iguales):

```text
Taquilla: TaquillaEspera · 5 rondas · 10 esperan · 6 devuelven
  E1 esperan en WAITING (sin espera activa)  OK
  E2 devolver() solo vale al dueño           OK
  E3 una devolución = un cliente servido     OK
  E4 sin asientos repetidos                  OK
  E5 interrupt() libera a los demás          OK
  E6 vendidas() y recaudado() cuadran        OK
VEREDICTO: ninguna regla rota (no demuestra que sea correcta)
```

Como salió OK a la primera, se rompió a propósito cambiando el `while` por un
`if` (`if (vendidas == aforo) cerrojo.wait()`). Resultado, igual en las tres
ejecuciones:

```text
Taquilla: TaquillaEspera · 5 rondas · 10 esperan · 6 devuelven
  E1 esperan en WAITING (sin espera activa)  OK
  E2 devolver() solo vale al dueño           OK
  E3 una devolución = un cliente servido     ROTA en 4/5 rondas
  E4 sin asientos repetidos                  OK
  E5 interrupt() libera a los demás          OK
  E6 vendidas() y recaudado() cuadran        OK
VEREDICTO: INCORRECTA
```

Además, por la consola salen muchas excepciones en los hilos que esperan:

```text
Exception in thread "espera-5" java.lang.ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 20
```

Por qué: cada `devolver` hace `notifyAll()` y despierta a los 10 que esperan,
pero solo hay un asiento libre. El primero que coge el cerrojo se lo lleva. Los
demás, con `if`, no vuelven a mirar la condición y siguen como si hubiera sitio:
`indexOfFirst { it == null }` devuelve -1 y `duenos[-1]` lanza la excepción, que
mata al hilo. Esos hilos se quedan sin entrada aunque luego se devuelvan más
asientos, así que se sirve a menos de 6 clientes → **E3**. E5 sale OK porque
los hilos muertos ya no están vivos, y E6 porque la excepción salta antes de
tocar las cuentas. En una de las cinco rondas el reparto coincidió y no se
notó, que es justo lo peligroso de estos fallos. Además, `wait()` puede
terminar sin que nadie avise (despertar espurio), otra razón para usar `while`.
Después se dejó otra vez con `while`.

> **Pendiente:** repetir la prueba en IntelliJ y hacer las capturas (versión
> buena y versión con `if`).

### d) ¿En qué estado deben estar los que esperan?

En **WAITING**: dormidos en `wait()` sin tiempo, sin gastar CPU, hasta que
alguien haga `notifyAll()` o los interrumpan. Si un hilo aparece en
**TIMED_WAITING** es que espera con tiempo (`wait(ms)` o `Thread.sleep(ms)` en
un bucle): se despierta cada poco a mirar si hay sitio aunque nadie haya
devuelto nada. Es una espera activa disfrazada: gasta CPU y cerrojo para nada y
tarda en enterarse. Puede vender bien, pero no espera como debe, y E1 lo
comprueba mirando el estado de los hilos a los 300 ms. Se probó cambiando
`cerrojo.wait()` por `cerrojo.wait(50)`: vende todo bien (E2 a E6 OK) pero
**E1 sale ROTA en 5/5 rondas**.

### e) ¿Podría usar `notify()` en lugar de `notifyAll()`?

> **Pendiente:** el enunciado pide preguntarlo en opencode (agente Plan). Por
> ahora se ha preguntado a **Codex**, en modo solo lectura.

Pregunta: «¿Podría usar notify() en lugar de notifyAll() en esta taquilla?».

Respuesta de Codex (resumida): técnicamente sí, porque todos esperan la misma
condición (`vendidas < aforo`) y cada `devolver` libera exactamente un asiento,
así que despertar a uno basta. El despertado tiene que volver a comprobar el
`while` porque otro comprador puede haberse llevado el asiento antes, y ninguna
de las dos opciones atiende por orden de llegada. Aun así recomienda mantener
`notifyAll()` «porque cumple `AGENTS.md`».

Valoración con el apartado 7.1: la parte técnica es correcta, pero la
conclusión no aporta nada: la regla de `AGENTS.md` que cita es la que se añadió
en el apartado a), así que solo repite lo que se le dijo. Tampoco explica en qué
caso dejaría de valer `notify()`, que es lo importante.

En esta taquilla, tal como está, `notify()` funcionaría: todos esperan a lo
mismo y cada devolución libera un asiento, y el `while` protege si un `comprar`
normal se adelanta (el despertado se vuelve a dormir y no queda nada libre).
Pero dejaría de valer con cambios bastante normales:

- Si se añade otro tipo de espera sobre el mismo cerrojo, por ejemplo
  `esperarEntradas(cliente, n)` para un grupo que quiere 3 asientos. Un
  `notify()` podría despertar al del grupo, que ve que solo hay 1 libre y se
  vuelve a dormir, y el aviso se pierde: el que quería 1 asiento sigue dormido
  con un asiento libre.
- Si una operación libera varios asientos a la vez (devolver varias entradas
  con un solo aviso): `notify()` despierta a uno aunque haya sitio para más.

Por eso la regla de los apuntes es usar `notifyAll()` salvo que se esté seguro
de que todos esperan a la misma condición y cada aviso sirve exactamente a uno.

---

## Cierre

> **Pendiente del alumno:** reflexión propia de cinco líneas como máximo. Hechos
> que se pueden usar:

- `TaquillaIA` se entregó como «completamente thread-safe» y rompe las cinco
  reglas: R2 y R3 en todas las rondas, R1, R4 y R5 en algunas.
- En la auditoría, Codex encontró todas las carreras reales y solo dio una que
  no rompe el contrato. Al revisar su respuesta se le marcó como error algo que
  era correcto, y fue el propio Codex quien lo hizo ver después: también hay que
  verificar lo que uno corrige.
- Con `notify()` acertó en la parte técnica, pero justificó la conclusión con la
  regla de `AGENTS.md` en lugar de razonarla.
- Momentos en los que se habría entregado código incorrecto sin verificar: al
  creer la frase de `TaquillaIA`, y en el Reto C con un `if` en lugar de `while`,
  que parece igual al leerlo y falla en 4 de cada 5 rondas.
