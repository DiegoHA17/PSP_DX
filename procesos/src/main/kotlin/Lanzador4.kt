
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

        if (orden.isBlank()) continue

        ejecutarOrden(orden)
    }
}

private fun ejecutarOrden(orden: String) {
    val salidaTemporal = File.createTempFile("lanzador-", ".txt")
    var codigo = -1
    var milisegundos = 0L

    try {
        // Unir stdout y stderr y redirigirlos a un fichero temporal.
        val proceso = ProcessBuilder(comandoShell(orden))
            .redirectErrorStream(true)
            .redirectOutput(salidaTemporal)
            .start()

        // Medir el tiempo desde que comienza la ejecución.
        val inicio = System.nanoTime()

        // Esperar hasta el límite de tiempo.
        val terminado = proceso.waitFor(TIMEOUT_S, TimeUnit.SECONDS)

        if (terminado) {
            codigo = proceso.exitValue()
        } else {
            println("Tiempo límite superado: se terminará el proceso.")

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
            codigo = -1
        }

        milisegundos = (System.nanoTime() - inicio) / 1_000_000

        // Mostrar la salida numerada.
        salidaTemporal.bufferedReader().use { lector ->
            var numeroLinea = 1

            while (true) {
                val linea = lector.readLine() ?: break
                println("$numeroLinea: $linea")
                numeroLinea++
            }
        }

        println("[código $codigo · $milisegundos ms]")

    } catch (e: Exception) {
        println("Error al ejecutar la orden: ${e.message}")
        codigo = -1
    } finally {
        // TODO 3.d: guardar cada ejecución en historial.txt.
        val historial = File("historial.txt")
        historial.appendText(
            "${orden.replace(';', ',')};$codigo;$milisegundos${System.lineSeparator()}"
        )

        salidaTemporal.delete()
    }
}

private fun comandoShell(orden: String): List<String> {
    val sistema = System.getProperty("os.name").lowercase()

    return if (sistema.contains("win")) {
        listOf("cmd.exe", "/c", orden)
    } else {
        listOf("/bin/sh", "-c", orden)
    }
}
