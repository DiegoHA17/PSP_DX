package es.dam.psp.ut1.practicas

import java.io.File
import java.util.concurrent.TimeUnit

/** Segundos máximos que puede durar una orden antes de terminarla. */
private const val TIMEOUT_S = 5L

/**
 * Código que asigna el Lanzador cuando no hay código de salida real:
 * orden terminada por superar TIMEOUT_S (3.c) o error al lanzarla.
 */
private const val CODIGO_SIN_SALIDA = -1

fun main() {
    println("=== Lanzador de órdenes ===")
    println("Escribe 'salir' para terminar.")

    while (true) {
        print("> ")
        val orden = readlnOrNull()?.trim() ?: break

        if (orden.equals("salir", ignoreCase = true)) {
            println("Fin del lanzador.")
            break
        }

        if (orden.isBlank()) {
            continue
        }

        ejecutarOrden(orden)
    }
}

private fun ejecutarOrden(orden: String) {
    val salidaTemporal = File.createTempFile("lanzador-", ".txt")

    var codigo = CODIGO_SIN_SALIDA
    var milisegundos = 0L

    try {
        // 3.a y 3.c: salida estándar y de error juntas, redirigidas a un
        // fichero temporal para que el padre nunca se bloquee leyendo.
        val proceso = ProcessBuilder(comandoShell(orden))
            .redirectErrorStream(true)
            .redirectOutput(salidaTemporal)
            .start()

        // 3.b: el tiempo se mide desde start().
        val inicio = System.nanoTime()

        // 3.c: esperar como máximo TIMEOUT_S segundos.
        val terminado = proceso.waitFor(TIMEOUT_S, TimeUnit.SECONDS)

        if (terminado) {
            // Código de salida real del proceso hijo.
            codigo = proceso.exitValue()
        } else {
            println("Tiempo límite de $TIMEOUT_S s superado: se termina el proceso y sus descendientes.")

            // Obtener los descendientes antes de terminar el proceso padre
            // (si el padre muere antes, sus hijos ya no cuelgan de él).
            val descendientes = proceso.toHandle()
                .descendants()
                .toList()
                .asReversed()

            for (descendiente in descendientes) {
                if (descendiente.isAlive) {
                    descendiente.destroyForcibly()
                }
            }

            proceso.destroyForcibly()
            proceso.waitFor()

            // El código real del hijo matado no importa (1 en Windows,
            // 137 en Linux): el Lanzador usa -1 para indicar timeout.
            codigo = CODIGO_SIN_SALIDA
        }

        milisegundos = (System.nanoTime() - inicio) / 1_000_000

        // 3.a: mostrar la salida numerada una vez terminado el hijo.
        salidaTemporal.bufferedReader().use { lector ->
            var numeroLinea = 1
            while (true) {
                val linea = lector.readLine() ?: break
                println("$numeroLinea: $linea")
                numeroLinea++
            }
        }

        // 3.b: código de salida y tiempo transcurrido.
        println("[código $codigo · $milisegundos ms]")

    } catch (e: Exception) {
        codigo = CODIGO_SIN_SALIDA
        println("Error al ejecutar la orden: ${e.message}")
        println("[código $codigo · $milisegundos ms]")
    } finally {
        // 3.d: registrar la ejecución con el formato orden;codigo;milisegundos.
        File("historial.txt").appendText(
            "${orden.replace(';', ',')};$codigo;$milisegundos${System.lineSeparator()}"
        )

        salidaTemporal.delete()
    }
}

/**
 * Construye la orden para la shell del sistema operativo
 * (cmd.exe /c en Windows, /bin/sh -c en Linux y macOS).
 */
private fun comandoShell(orden: String): List<String> {
    val sistema = System.getProperty("os.name").lowercase()

    return if (sistema.contains("win")) {
        listOf("cmd.exe", "/c", orden)
    } else {
        listOf("/bin/sh", "-c", orden)
    }
}
