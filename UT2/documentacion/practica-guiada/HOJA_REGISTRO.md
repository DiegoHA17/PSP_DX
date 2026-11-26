# Hoja de registro · UT-2 Práctica guiada con IA

0490 · Programación de Servicios y Procesos · 2.º DAM · Curso 2026-27

**Alumno:** [PENDIENTE] · **Fecha:** [PENDIENTE] · **Modelo de opencode:** [PENDIENTE]

> Versión en Markdown de la hoja de registro; el Word que se entrega es
> `UT2_Hoja_de_registro.docx` (mismo contenido). Lo marcado como **[PENDIENTE]**
> lo completa el alumno siguiendo [`PENDIENTES.md`](PENDIENTES.md). Los datos
> rellenos son de ejecuciones reales hechas por Claude en la terminal (salidas
> completas en [`evidencias/`](evidencias/README.md)); las respuestas de IA y el
> código de los Retos B y C son de Codex (opencode aún no se ha usado).

| Reto | Qué debe aparecer (enunciado) |
|------|-------------------------------|
| A | Captura de una ejecución del verificador con `TaquillaIA`. Tabla de reglas rotas en tres ejecuciones. Intercalados de cada fallo. Tabla yo / IA / falso positivo. Respuesta al apartado d. |
| B | Tu prompt. Número de vueltas hasta que el verificador sale OK y captura final. Lista de comprobación respondida. Respuesta al apartado e con la tabla de tiempos. |
| C | Tu regla de `AGENTS.md`. Captura de `VerificadorEspera` (y de la versión rota a propósito, si la hiciste). Respuestas a d y e. |
| Cierre | En cinco líneas como máximo: ¿qué ha hecho bien la IA y qué no? ¿En qué momento habrías entregado código incorrecto si hubieras aceptado lo que te dio sin verificarlo? |
| Enlace | Enlace a tu repositorio, con un commit por reto. |

---

## Reto A

**Captura de una ejecución con `TaquillaIA`:** [PENDIENTE: captura 1, con el usuario visible]

**Reglas rotas en tres ejecuciones** (ejecuciones de Claude en la terminal; sustituir por las propias de IntelliJ):

| Regla | Ejec. 1 | Ejec. 2 | Ejec. 3 |
|-------|:-------:|:-------:|:-------:|
| R1 todo o nada | 7/30 | 4/30 | 2/30 |
| R2 asiento repetido | 30/30 | 30/30 | 30/30 |
| R3 sobreventa | 30/30 | 30/30 | 28/30 |
| R4 `vendidas()` | 8/30 | 4/30 | 2/30 |
| R5 `recaudado()` | 13/30 | 6/30 | 6/30 |

No sale lo mismo cada vez porque el orden en que se intercalan los hilos lo
decide el planificador del sistema operativo: una carrera solo aparece si dos
hilos coinciden justo en la zona peligrosa.

**Intercalados** (líneas de `TaquillaIA.kt`):

| Fallo | Hilo 1 | Hilo 2 | Regla |
|-------|--------|--------|-------|
| Asiento repetido (36-37) | 1. `containsKey(5)` → false · 3. `asientos[5] = c1` | 2. `containsKey(5)` → false · 4. `asientos[5] = c2` | R2 |
| Compra parcial (30, 43) | Con `contador = 197`: 1. comprueba 197+2 ≤ 200 · 3. ocupa 197 y 198 · 5. suma 2 | 2. comprueba 197+2 ≤ 200 · 4. solo encuentra el 199 · 6. suma 2 → 201 | R1, R4, R5 |
| Sobreventa (fallos 1 y 2 juntos) | Con 199 vendidos y libre el 199: 1. comprueba 199+1 ≤ 200 · 3. `containsKey(199)` → false · 5. lo ocupa | 2. comprueba 199+1 ≤ 200 · 4. `containsKey(199)` → false · 6. lo ocupa | R3 (201 entregados) y R2 |
| Caja no atómica (44) | 1. lee 1000 · 3. escribe 1050 | 2. lee 1000 · 4. escribe 1075 | R5 |
| Visibilidad de caja (26, 50) | 2. compra y escribe `caja = 25` | 1. lee 0 · 3. puede seguir leyendo 0 | R5 (el verificador no lo ve) |

**Tabla yo / IA / falso positivo** (IA = Codex, mismo prompt del enunciado):

| Fallo | Lo encontré yo | Lo encontró la IA | ¿Real o falso positivo? |
|-------|:--------------:|:-----------------:|-------------------------|
| `containsKey` + `put` | [PENDIENTE] | Sí | Real (R2) |
| Comprobar aforo y vender por separado | [PENDIENTE] | Sí | Real: R3, junto con el asiento repetido (Codex lo explica bien) |
| Compra parcial (`addAndGet(n)`) | [PENDIENTE] | Sí | Real (R1, R4, R5) |
| `caja +=` | [PENDIENTE] | Sí | Real (R5) |
| Lecturas separadas de `vendidas()` y `recaudado()` | [PENDIENTE] | Sí | Falso positivo: el contrato no pide leerlas a la vez |
| Visibilidad de `caja` | [PENDIENTE] | Sí | Real, el verificador no lo detecta |

[PENDIENTE: repetir la auditoría con opencode en modo Plan y completar o sustituir la columna «IA»]

**Apartado d:** `AtomicInteger` y `ConcurrentHashMap` solo hacen atómica cada
operación suelta, pero `comprar` encadena varias (comprobar, buscar, ocupar,
sumar) que tendrían que hacerse de una vez, y `caja` ni siquiera es atómica.
R3 es consecuencia de los otros dos fallos juntos: los asientos van de 0 a 199,
así que solo se pasa de 200 repitiendo (R2), y además varios hilos tienen que
pasar a la vez la comprobación del aforo, porque si no el contador frenaría las
ventas al llegar a 200. R4 es consecuencia de la compra parcial. R5 tiene causa propia
(la caja no atómica) además de la compra parcial.

---

## Reto B

**Prompt:** el de `PENDIENTES.md`, Reto B a), enviado a **Codex** (modo
escritura, rama de pruebas) con «cumple AGENTS.md, no compiles ni ejecutes».
[PENDIENTE: si se repite en opencode Build, poner el prompt que se envíe de verdad]

**Ficheros que tocó la IA:** solo `TaquillaSegura.kt`; no hubo que deshacer nada
(`evidencias/retoB/codex_TaquillaSegura.patch`).

**Número de vueltas:** 1 (el código de Codex pasó a la primera; la versión anterior
de Claude, también)

**Captura final:** [PENDIENTE: captura 2, `TaquillaSegura` con R1-R5 OK]

Resultado real (código de Codex, tres ejecuciones, `evidencias/retoB/B1` a `B3`):
R1-R5 OK en las tres.

**Lista de comprobación** (sobre el código de Codex, que es el de la rama):

| Pregunta | Respuesta |
|----------|-----------|
| Sección crítica y cerrojo | Todo `comprar`, desde la comprobación de libres hasta sumar la caja, con `synchronized(cerrojo)`. `asientos`, `contador` y `caja` solo se tocan con ese cerrojo |
| «Comprobar y actuar» fuera del cerrojo | No; todo el cuerpo de `comprar` está dentro, también `if (n <= 0 \|\| n > aforo - contador)` |
| Lecturas | `vendidas()` y `recaudado()` usan el mismo cerrojo; sin él podría verse un valor antiguo |
| Herramientas mezcladas | No; dentro del cerrojo solo hay un mapa normal y variables normales |
| Varios cerrojos | No, uno solo |

**Apartado e:** el verificador solo prueba los intercalados que salen en 30
rondas, así que no demuestra nada. Ejemplo que la lista ve y el verificador no:
si `recaudado()` leyera sin cerrojo seguiría saliendo OK, porque solo se llama
después de `join()`.

| Taquilla | Ejec. 1 | Ejec. 2 | Ejec. 3 | Media |
|----------|--------:|--------:|--------:|------:|
| `TaquillaIA` | 185,5 ms | 161,2 ms | 163,9 ms | 170,2 ms |
| `TaquillaSegura` | 171,0 ms | 151,6 ms | 161,4 ms | 161,3 ms |

(Una tanda seguida, `evidencias/retoB/tiempos/` y `B1` a `B3`.)
[PENDIENTE: sustituir por los tiempos de IntelliJ si se repiten las mediciones]

La corrección no ha salido más cara; con tres ejecuciones y tanta variación no
se puede concluir más.

---

## Reto C

**Regla de `AGENTS.md`:** Cuando un hilo tenga que esperar a que cambie el
estado (por ejemplo, a que haya un asiento libre), usa `wait()` sin tiempo
dentro de `synchronized` sobre el mismo cerrojo que protege ese estado, y
siempre dentro de un `while` que vuelva a comprobar la condición al despertar.
No uses `Thread.sleep`, `wait(milisegundos)` ni bucles que miren el estado una y
otra vez: el hilo debe quedar en estado WAITING. Quien cambie el estado
(devolver un asiento) avisa con `notifyAll()`. Si interrumpen al hilo mientras
espera, captura `InterruptedException` y devuelve `null`.

**Captura de `VerificadorEspera`:** [PENDIENTE: captura 3, E1-E6 OK]

**Versión rota a propósito (`while` → `if`):** [PENDIENTE: captura 4]

Resultado real con el código de Codex (`TaquillaEspera` OK: E1-E6 en las tres
ejecuciones, `evidencias/retoC/C1` a `C3`). Con `if`: E3 rota en 4/5 rondas
(igual en las tres ejecuciones, `C1_if_roto` a `C3_if_roto`) y excepciones
`NoSuchElementException: List is empty`. Con `if`, los hilos que despierta
`notifyAll()` no vuelven a comprobar si queda sitio: `comprar(cliente, 1)`
devuelve una lista vacía, `.first()` lanza la excepción y el hilo muere sin
entrada, así que se sirve a menos de 6.

**Apartado d:** deben estar en WAITING, dormidos en `wait()` sin tiempo hasta un
`notifyAll()` o una interrupción. TIMED_WAITING significa que esperan con tiempo
(`wait(ms)` o `sleep`) y se despiertan cada poco a mirar aunque nadie haya
devuelto nada: es espera activa. Probado con `wait(50)`: E2-E6 OK pero E1 rota
en 5/5 rondas (`evidencias/retoC/C_wait50`).

**Apartado e:** [PENDIENTE: respuesta de opencode en modo Plan]

Respuesta de Codex: sí podría, porque todos esperan lo mismo y cada devolución
libera un asiento, pero recomienda `notifyAll()` «porque cumple `AGENTS.md`».
Valoración: la parte técnica es correcta, pero se justifica con la regla que
pusimos nosotros y no dice cuándo dejaría de valer: con otra espera distinta
sobre el mismo cerrojo (un grupo que espera 3 asientos) o si se liberan varios
asientos con un solo aviso.

---

## Cierre

[PENDIENTE: reflexión propia, cinco líneas como máximo]

---

## Enlace

- Repositorio: https://github.com/DiegoHA17/PSP_DX
- Pull request: https://github.com/DiegoHA17/PSP_DX/pull/8
- Commits por reto: código de partida `710aacc` · Reto A `c0c91de` · Reto B
  `a3cc316` (versión de Claude) y `fda5ebe` (código de Codex) · Reto C `62160d1`
  (versión de Claude) y `ec72d97` (código de Codex)
