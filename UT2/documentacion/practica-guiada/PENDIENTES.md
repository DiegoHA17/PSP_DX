# Pendientes para terminar la práctica guiada

Guía paso a paso de lo que falta y que tiene que hacer el alumno: usar opencode,
revisar los diffs en IntelliJ, hacer las capturas y escribir la reflexión. El
código de la rama `feat/ut2-practica-guiada` ya funciona y está verificado; esta
guía no lo cambia.

Lo que se apunta en cada paso va a [`HOJA_REGISTRO.md`](HOJA_REGISTRO.md) y al
Word `UT2_Hoja_de_registro.docx`, en los huecos marcados con **[PENDIENTE]**.

## 0. Preparación (una vez)

1. **Carpeta de pruebas.** opencode tiene que partir de los esqueletos vacíos, así
   que se trabaja en una copia aparte del repositorio (un *worktree*) creada desde
   el commit del código de partida. Tu carpeta `PSP` no cambia: la versión
   verificada y esta guía siguen ahí. En una terminal (vale la de IntelliJ):

   ```powershell
   cd C:\Users\DIEGO\Desktop\PSP
   git worktree add ..\PSP-opencode -b prueba/ut2-opencode 710aacc
   ```

   Se crea `C:\Users\DIEGO\Desktop\PSP-opencode`. Ahí, `TaquillaSegura.kt` y
   `TaquillaEspera.kt` tienen `TODO(...)`, `fabrica` apunta a `::TaquillaIA` y
   `AGENTS.md` no tiene todavía la regla del alumno. La rama
   `prueba/ut2-opencode` **no se sube ni se fusiona**: solo sirve para la sesión
   con opencode y las capturas.

2. **IntelliJ.** File > Open > `C:\Users\DIEGO\Desktop\PSP-opencode\UT2\hilos`
   (la copia de pruebas, no la carpeta `PSP`). Espera a que Gradle termine de
   cargar (barra de abajo). En el árbol tiene que aparecer
   `src/main/kotlin/es/dam/psp/ut2/guiada/`. Esta guía la puedes tener abierta
   desde la carpeta `PSP` o desde GitHub.

3. **opencode.** Instálalo desde opencode.ai/docs (en Windows, el zip de la página
   de descargas). Cierra y vuelve a abrir IntelliJ. Abre el terminal de IntelliJ
   (Alt+F12): tiene que estar en `...\UT2\hilos`, que es donde está `AGENTS.md`.
   Ejecuta `opencode`, escribe `/models` y elige un modelo marcado como **Free**.
   Apunta el nombre del modelo. La tecla **Tab** cambia entre **Plan** (solo lee)
   y **Build** (edita ficheros).

4. **Que se vea tu usuario en las capturas.** En la ventana Run de IntelliJ, la
   primera línea es la orden `java` con la ruta `C:\Users\DIEGO\...`: haz las
   capturas sin recortar esa línea. Si no sale, ejecuta antes `whoami` en el
   terminal de IntelliJ (funciona en PowerShell y en cmd) y que salga en la
   misma captura.

**Regla para todo:** antes de cada petición a opencode, haz un commit (Ctrl+K)
en la rama de pruebas. Así la ventana Commit solo muestra lo que ha hecho esa
petición y puedes deshacerlo con botón derecho > Rollback.

## 1. Reto A (sin IA, salvo el apartado c)

### a) Tres ejecuciones con `TaquillaIA`

1. Abre `guiada/Verificador.kt`. Comprueba que la línea 19 es:
   ```kotlin
   val fabrica: (Int) -> Taquilla = ::TaquillaIA
   ```
2. Pulsa el ▶ verde junto a `fun main()` (línea 68) > Run. Hazlo **3 veces**.
3. Apunta, de cada ejecución, el «ROTA en X/30» de R1 a R5 y la línea «Primer fallo».
4. **Captura 1:** la ventana Run de una de las tres ejecuciones, con la orden `java`
   arriba (tu usuario) y el «VEREDICTO: INCORRECTA» abajo.

Los números no tienen por qué coincidir con los del README: cambian en cada
ejecución. En la hoja van los tuyos.

### b) y d)

Ya están en el README (intercalados y relación entre reglas). Léelos y
asegúrate de que puedes explicarlos con tus palabras.

### c) Auditoría con opencode (modo **Plan**)

1. En opencode, pulsa Tab hasta que ponga **Plan**.
2. Pega este prompt tal cual:
   ```text
   Analiza guiada/TaquillaIA.kt frente al contrato de guiada/Taquilla.kt. Enumera cada condición de carrera: líneas implicadas, intercalado de dos hilos que la produce y qué regla del contrato rompe. No modifiques ningún fichero.
   ```
3. Copia la respuesta entera a un fichero de texto aparte (no al repositorio).
4. Rellena la tabla «fallo | lo encontré yo | lo encontró la IA | ¿real o falso
   positivo?» de la hoja con lo que diga **opencode**. Para valorar cada punto
   usa los criterios del README:
   - un fallo es «real» si tiene un intercalado posible y rompe una regla;
   - es «mal atribuido» si la regla rota en realidad la causa otro fallo (por
     ejemplo, R3 necesita a la vez asientos repetidos y que varios hilos pasen
     juntos la comprobación del aforo; si la IA lo achaca solo a uno, está
     incompleto);
   - es «falso positivo» si no rompe ninguna regla del contrato.
5. Si quieres, deja también la comparación con Codex que ya está en la hoja.

## 2. Reto B

### a) Prompt en opencode (modo **Build**)

1. Haz commit en la rama de pruebas (Ctrl+K, mensaje «antes del reto B»).
2. Tab hasta **Build** y pega:
   ```text
   Implementa la clase TaquillaSegura en guiada/TaquillaSegura.kt cumpliendo el contrato de guiada/Taquilla.kt. comprar(cliente, n) es todo o nada: si no quedan n asientos libres no vende ninguno y devuelve una lista vacía; si quedan, los vende todos a ese cliente, suma n a vendidas y n × PRECIO a la caja. En TaquillaIA encontré estos fallos y no deben repetirse: (1) containsKey y put sobre el mapa no son atómicos y dos hilos se llevan el mismo asiento; (2) se comprueba el aforo con contador.get() y se vende después, así que dos hilos pasan la comprobación y uno recibe menos asientos de los pedidos; (3) contador.addAndGet(n) suma lo pedido aunque se entreguen menos; (4) caja += n * PRECIO no es atómico y se pierden ventas; (5) recaudado() lee caja sin sincronizar. Usa un solo cerrojo con synchronized sobre un objeto privado que proteja los tres datos (dueño de cada asiento, vendidas y caja). La comprobación de que quedan n libres y la venta tienen que estar dentro del mismo bloque synchronized. vendidas() y recaudado() también deben leer con el cerrojo. No uses AtomicInteger ni ConcurrentHashMap dentro del synchronized. Modifica solo TaquillaSegura.kt y explica qué invariante protege el cerrojo.
   ```
3. Si cambias algo del prompt, copia en la hoja **el que envíes de verdad**.

### b) Revisar el diff antes de aceptar

1. Ventana Commit (Ctrl+K). Mira qué ficheros aparecen como modificados.
   - Solo debería salir `TaquillaSegura.kt`. Si sale `Taquilla.kt`, `TaquillaIA.kt`,
     `Verificador.kt`, `VerificadorEspera.kt` o `build.gradle.kts`: botón derecho >
     Rollback sobre ese fichero y dile a opencode que no puede tocarlo (AGENTS.md).
2. Doble clic en `TaquillaSegura.kt` para ver el diff. Comprueba:
   - que solo usa cosas de AGENTS.md (`synchronized`, `@Volatile`, atómicas,
     `ReentrantLock`…) y nada de corrutinas ni librerías;
   - que la comprobación de «quedan n libres» está **dentro** del `synchronized`;
   - que `vendidas()` y `recaudado()` también usan el cerrojo.
3. Apunta en la hoja qué ficheros tocó y si tuviste que deshacer algo.

### c) Verificador con `TaquillaSegura`

1. En `Verificador.kt`, cambia **solo** la línea 19 a:
   ```kotlin
   val fabrica: (Int) -> Taquilla = ::TaquillaSegura
   ```
2. ▶ junto a `main`, **3 veces**. Tiene que salir R1–R5 OK y
   «VEREDICTO: ninguna regla rota en 30 rondas (no demuestra que sea correcta)».
3. Si alguna regla sale ROTA: copia la salida completa, pégasela a opencode
   (Build) y pídele que lo corrija. Vuelve al paso b. Cuenta cuántas veces lo
   haces: eso son las **vueltas** (si sale bien a la primera, es 1).
4. Apunta el tiempo de cada ejecución (lo pone la primera línea: «tiempo …ms»).
5. **Captura 2:** la ejecución final OK, con tu usuario visible.

### d) Lista de comprobación

Contesta las cinco preguntas de la lista **sobre el código que generó opencode**.
Si coincide con el de la rama principal, valen las respuestas del README; si es
distinto, revisa cada pregunta mirando su código.

### e) Tiempos

Para comparar en las mismas condiciones, ejecuta 3 veces con `::TaquillaIA` y
3 con `::TaquillaSegura`, una tanda detrás de otra, y apunta los seis tiempos en
la tabla de la hoja. Al terminar, deja la línea con `::TaquillaSegura`.

## 3. Reto C

### a) Regla en AGENTS.md

1. Abre `hilos/AGENTS.md` y, debajo de `<!-- Reto C, apartado a: añade aquí tu regla -->`,
   pega (o escribe con tus palabras) la regla que está en el README, apartado C a).
2. Guarda y haz commit («regla del reto C»). Copia en la hoja la regla tal como
   la dejes.

### b) Prompt en opencode (modo **Build**)

1. Sal de opencode y vuelve a entrar (`/exit` y `opencode`) para que lea la
   regla nueva de AGENTS.md.
2. Tab hasta **Build** y pega:
   ```text
   Implementa TaquillaEspera en guiada/TaquillaEspera.kt cumpliendo TaquillaConEspera de guiada/Taquilla.kt. comprar, vendidas y recaudado igual que en TaquillaSegura (un solo cerrojo para dueños, vendidas y caja). devolver(cliente, asiento) solo funciona si el asiento es de ese cliente: lo libera, resta 1 a vendidas, devuelve 25 € de la caja y avisa a los que esperan. esperarEntrada(cliente) espera mientras no haya asientos libres y luego vende uno. Sigue las reglas de AGENTS.md sobre cómo esperar. Modifica solo TaquillaEspera.kt.
   ```
3. Ventana Commit: revisa el diff igual que en el Reto B. Además comprueba que
   espera con `wait()` **dentro de un `while`**, que `devolver` llama a
   `notifyAll()` y que, al capturar `InterruptedException`, devuelve `null`.

### c) `VerificadorEspera`

1. Abre `guiada/VerificadorEspera.kt`. La línea 16 tiene que ser (no hay que cambiarla):
   ```kotlin
   val fabricaEspera: (Int) -> TaquillaConEspera = ::TaquillaEspera
   ```
2. ▶ junto a `main` (línea 85), **3 veces**. Apunta qué reglas E1–E6 salen OK o ROTA.
3. **Captura 3:** una ejecución, con tu usuario visible.
4. Si alguna regla sale ROTA: explica en la hoja qué hacía mal el código, corrígelo
   (a mano o con opencode) y vuelve a ejecutar.

**Romperlo a propósito con `if`** (si todo salió OK a la primera):

1. En `TaquillaEspera.kt`, busca la línea de la espera. Si opencode lo escribió
   como en la rama principal, es:
   ```kotlin
   while (vendidas == aforo) cerrojo.wait()
   ```
   Cámbiala por:
   ```kotlin
   if (vendidas == aforo) cerrojo.wait()
   ```
   (si sus nombres son otros, cambia solo la palabra `while` por `if`).
2. ▶ `VerificadorEspera` **3 veces**. Apunta qué reglas se rompen. Lo esperable
   es E3 rota en algunas rondas y excepciones `ArrayIndexOutOfBoundsException`
   en la consola (el README explica por qué).
3. **Captura 4:** la ejecución con `if`, con las reglas rotas y tu usuario visible.
4. **Restaura el `while`:** vuelve a escribir `while` o, en la ventana Commit,
   botón derecho sobre `TaquillaEspera.kt` > Rollback (si hiciste commit antes).
   Ejecuta otra vez para comprobar que vuelve a salir todo OK.

### d) Estado de los hilos

La respuesta está en el README (WAITING frente a TIMED_WAITING). Léela y
redáctala con tus palabras en la hoja.

### e) Pregunta en opencode (modo **Plan**)

1. Tab hasta **Plan** y pega:
   ```text
   ¿Podría usar notify() en lugar de notifyAll() en esta taquilla?
   ```
2. Copia la respuesta. En la hoja, resúmela y valórala con el apartado 7.1:
   - ¿dice bien que aquí funcionaría porque todos esperan lo mismo y cada
     devolución libera un asiento?
   - ¿explica cuándo dejaría de valer? (otra espera distinta sobre el mismo
     cerrojo, o liberar varios asientos con un solo aviso). Si no lo explica, dilo.
   - ¿justifica la respuesta solo con AGENTS.md, como hizo Codex?

## 4. Hoja de registro y entrega

1. Abre `UT2_Hoja_de_registro.docx` (en esta carpeta). Rellena tu nombre y la fecha.
2. Sustituye cada **[PENDIENTE]**:

   | Reto | Qué completar |
   |------|---------------|
   | A | Captura 1 · tus tres ejecuciones en la tabla · tabla yo / opencode / falso positivo |
   | B | Prompt enviado · ficheros que tocó opencode · número de vueltas · captura 2 · tus tiempos |
   | C | Regla tal como quedó en AGENTS.md · captura 3 y captura 4 · respuesta de opencode sobre `notify()` y tu valoración |
   | Cierre | Reflexión propia, **cinco líneas como máximo** (en el README tienes hechos que puedes usar) |
   | Enlace | Ya está puesto el repositorio y el PR #8 |

3. Indica en la hoja qué modelo gratuito de opencode usaste.
4. Guarda como Word y exporta a PDF (Archivo > Guardar como > PDF). Adjunta el
   documento a la tarea.
5. **Compara el código de opencode con el del PR.** Las capturas de los Retos B
   y C son del código de opencode, y el repositorio tiene la versión de Claude.
   Desde la carpeta `PSP`:
   ```powershell
   git diff feat/ut2-practica-guiada prueba/ut2-opencode -- UT2/hilos/src
   ```
   (haz antes un commit en `PSP-opencode` con el código final de opencode, con
   `while` restaurado). Si solo cambian detalles, déjalo así y dilo en la hoja.
   Si prefieres entregar la versión de opencode, pide que se pase a la rama del
   PR y se vuelvan a ejecutar los verificadores antes de fusionar.
6. **No borres la rama de pruebas** hasta que la práctica esté corregida: es la
   prueba de lo que hizo opencode. Cuando ya no la necesites:
   ```powershell
   git worktree remove ..\PSP-opencode
   git branch -D prueba/ut2-opencode
   ```
7. **Sube la documentación y fusiona.** Los cambios de esta revisión (README,
   PENDIENTES, HOJA_REGISTRO y la hoja en Word/PDF) están en un commit local de
   `feat/ut2-practica-guiada` que hay que subir (`git push`) para que aparezcan
   en el PR #8. Cuando GitHub Actions esté en verde, fusiona el PR («Create a
   merge commit»). Al fusionarlo se cierran los Issues #4, #5, #6 y #7.
