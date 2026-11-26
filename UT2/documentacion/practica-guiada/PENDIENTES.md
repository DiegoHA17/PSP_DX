# Lo que falta: capturas, revisión personal y entrega

Todo lo demás está hecho y documentado: el código de los Retos B y C (generado
por Codex y revisado por Claude Code), las ejecuciones reales en `evidencias/`,
las respuestas de Codex y la hoja de registro. Quedan cuatro capturas en
IntelliJ y los dos apartados personales.

La hoja que se entrega está en `entrega/` (`UT2_Hoja_de_registro.docx` y `.pdf`),
ya con el nombre y la fecha. Esa carpeta está en `.gitignore` y **no se sube al
repositorio**, porque lleva el nombre y las capturas con la ruta del usuario.

## 0. Preparación

1. En IntelliJ: File > Open > `C:\Users\DIEGO\Desktop\PSP\UT2\hilos`. Abajo a la
   derecha tiene que poner la rama `feat/ut2-practica-guiada`. Espera a que
   Gradle termine de cargar.
2. **Usuario visible en las capturas:** abre el panel Project (Alt+1). La primera
   línea del árbol es `hilos  C:\Users\DIEGO\Desktop\PSP\UT2\hilos`. Haz las
   cuatro capturas de **toda la ventana** de IntelliJ, con esa línea y la
   ventana Run a la vista.
3. Para recortar la pantalla, usa Win+Shift+S. Guarda las capturas en
   `C:\Users\DIEGO\Desktop\PSP\UT2\documentacion\practica-guiada\entrega\capturas\`
   con los nombres que se indican abajo.
4. Los cambios de las capturas 1 y 4 son **temporales: no hagas commit**. Al
   terminar, el código tiene que quedar como está ahora (paso 5).

## 1. Captura 1 · Reto A, `TaquillaIA`

1. Abre `src/main/kotlin/es/dam/psp/ut2/guiada/Verificador.kt`.
2. **Línea 19**, cambia
   ```kotlin
   val fabrica: (Int) -> Taquilla = ::TaquillaSegura
   ```
   por
   ```kotlin
   val fabrica: (Int) -> Taquilla = ::TaquillaIA
   ```
3. Pulsa el ▶ verde junto a `fun main()` (**línea 68**) > Run 'VerificadorKt'.
4. Tiene que salir `Taquilla: TaquillaIA`, varias reglas `ROTA en X/30 rondas` y
   `VEREDICTO: INCORRECTA`. Los números no coincidirán con la tabla de la hoja,
   porque cambian en cada ejecución; no pasa nada.
5. Captura → `captura1_TaquillaIA.png`.

## 2. Captura 2 · Reto B, `TaquillaSegura`

1. En `Verificador.kt`, **línea 19**, vuelve a dejar
   ```kotlin
   val fabrica: (Int) -> Taquilla = ::TaquillaSegura
   ```
2. ▶ junto a `fun main()` (línea 68) > Run.
3. Tiene que salir R1 a R5 `OK` y
   `VEREDICTO: ninguna regla rota en 30 rondas (no demuestra que sea correcta)`.
4. Captura → `captura2_TaquillaSegura.png`.

## 3. Captura 3 · Reto C, `VerificadorEspera`

1. Abre `src/main/kotlin/es/dam/psp/ut2/guiada/VerificadorEspera.kt`. **No hay que
   cambiar nada**: la línea 16 ya es
   `val fabricaEspera: (Int) -> TaquillaConEspera = ::TaquillaEspera`.
2. ▶ junto a `fun main()` (**línea 85**) > Run 'VerificadorEsperaKt'. Tarda unos
   segundos.
3. Tiene que salir E1 a E6 `OK` y
   `VEREDICTO: ninguna regla rota (no demuestra que sea correcta)`.
4. Captura → `captura3_VerificadorEspera.png`.

## 4. Captura 4 · Reto C, versión rota con `if`

1. Abre `src/main/kotlin/es/dam/psp/ut2/guiada/TaquillaEspera.kt`.
2. **Línea 51**, cambia solo la palabra `while` por `if`:
   ```kotlin
           while (contador >= aforo) {
   ```
   pasa a ser
   ```kotlin
           if (contador >= aforo) {
   ```
3. ▶ junto a `fun main()` de `VerificadorEspera.kt` (línea 85) > Run.
4. Salen muchas excepciones en rojo
   (`java.util.NoSuchElementException: List is empty.`, en
   `TaquillaEspera.kt:60`) y al final el resumen, con
   `E3 una devolución = un cliente servido  ROTA en X/5 rondas` y
   `VEREDICTO: INCORRECTA`. Baja hasta el final de la ventana Run para que se
   vean a la vez algunas excepciones y el resumen.
5. Captura → `captura4_if_roto.png`.
6. **Restaura el `while`** en la línea 51 (Ctrl+Z, o botón derecho sobre el
   fichero > Git > Rollback).

## 5. Comprobar que el código ha quedado igual

En el terminal de IntelliJ (Alt+F12):

```powershell
git status --short UT2/hilos
```

No tiene que salir nada. Si sale `Verificador.kt` o `TaquillaEspera.kt`, haz
Rollback de ese fichero (botón derecho > Git > Rollback).

## 6. Completar la hoja de `entrega/`

1. Abre `entrega/UT2_Hoja_de_registro.docx`.
2. En cada recuadro «Espacio para la captura N», borra el texto de dentro y
   pon la imagen (Insertar > Imágenes > Este dispositivo).
3. **[REVISIÓN PERSONAL]** Reto A c): en la columna «Mi revisión», pon Sí o No
   según lo que encontraste tú al leer `TaquillaIA.kt` antes de mirar la
   respuesta de Codex.
4. **[REFLEXIÓN PERSONAL]** Cierre: cinco líneas como máximo. Qué ha hecho bien
   la IA y qué no, y en qué momento habrías entregado código incorrecto sin
   verificar. En el README, apartado «Cierre», hay datos que puedes usar.
5. Guarda y exporta a PDF (Archivo > Guardar como > PDF) en la misma carpeta
   `entrega/`, sustituyendo el que hay. Adjunta el PDF a la tarea.

## 7. Subir y fusionar (cuando lo autorices)

Los commits locales de `feat/ut2-practica-guiada` se suben con `git push` para
que aparezcan en el PR #8, y cuando GitHub Actions esté en verde se fusiona.
`entrega/` no se sube. La rama de pruebas `prueba/ut2-opencode` (worktree
`..\PSP-opencode`) conserva lo que generó Codex: no la borres hasta que la
práctica esté corregida.
