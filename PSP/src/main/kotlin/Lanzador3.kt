
package es.dam.psp.ut1.practicas

import java.io.File
import java.util.concurrent.TimeUnit

private const val TIMEOUT_S = 5L

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

    var codigo = -1
    var milisegundos = 0L
    val inicio: Long

    try {
        // TODO 3.a: combinar la salida estándar y la salida de error.
        val proceso = ProcessBuilder(comandoShell(orden))
            .redirectErrorStream(true)
            .redirectOutput(salidaTemporal)
            .start()

        // TODO 3.b: medir el tiempo desde el inicio del proceso.
        inicio = System.nanoTime()

        // TODO 3.c: esperar como máximo TIMEOUT_S segundos.
        val terminado = proceso.waitFor(TIMEOUT_S, TimeUnit.SECONDS)

        if (terminado) {
            codigo = proceso.exitValue()
        } else {
            println("Tiempo límite superado: se terminará el proceso.")

            // Obtener los descendientes antes de terminar el proceso padre.
            val descendientes = proceso.toHandle()
                .descendants()
                .toList()
                .asReversed()

            // Terminar los descendientes y después el proceso principal.
            for (descendiente in descendientes) {
                if (descendiente.isAlive) {
                    descendiente.destroyForcibly()
                }
            }

            proceso.destroyForcibly()
            proceso.waitFor()

            codigo = -1
        }

        milisegundos = (System.nanoTime() - inicio) / 1_000_000

        // TODO 3.a: mostrar la salida numerada después de finalizar.
        salidaTemporal.bufferedReader().use { lector ->
            var numeroLinea = 1
            while (true) {
                val linea = lector.readLine() ?: break
                println("$numeroLinea: $linea")
                numeroLinea++
            }
        }

        // TODO 3.b: mostrar el código y el tiempo transcurrido.
        println("[código $codigo · $milisegundos ms]")

    } catch (e: Exception) {
        println("Error al ejecutar la orden: ${e.message}")
        println("[código -1 · $milisegundos ms]")
    } finally {
        // TODO 3.d: registrar la orden en el historial.
        File("historial.txt").appendText(
            "${orden.replace(';', ',')};$codigo;$milisegundos${System.lineSeparator()}"
        )

        salidaTemporal.delete()
    }
}

/**
 * Ejecuta una orden usando la shell del sistema operativo.
 */
private fun comandoShell(orden: String): List<String> {
    val sistema = System.getProperty("os.name").lowercase()

    return if (sistema.contains("win")) {
        listOf("cmd.exe", "/c", orden)
    } else {
        listOf("/bin/sh", "-c", orden)
    }
}
