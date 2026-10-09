

package es.dam.psp.ut1.practicas

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

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
        // TODO 3.a: preparar el proceso y unir stdout y stderr.
        val constructor = ProcessBuilder(comandoShellWindows(orden))
            .redirectErrorStream(true)
            .redirectOutput(salidaTemporal)

        val proceso = constructor.start()

        // TODO 3.b: medir el tiempo desde que comienza la ejecución.
        val inicio = System.nanoTime()

        // TODO 3.c: esperar sin leer directamente la salida del proceso.
        val terminado = proceso.waitFor(TIMEOUT_S, TimeUnit.SECONDS)

        if (terminado) {
            codigo = proceso.exitValue()
        } else {
            // Terminar primero los descendientes y después el proceso.
            proceso.toHandle().descendants().forEach { hijo ->
                hijo.destroyForcibly()
            }
            proceso.destroyForcibly()
            proceso.waitFor()
            codigo = -1
            println("La orden ha superado el límite de $TIMEOUT_S segundos.")
        }

        milisegundos = (System.nanoTime() - inicio) / 1_000_000

        // TODO 3.a: mostrar las líneas numeradas.
        salidaTemporal.useLines { lineas ->
            lineas.forEachIndexed { indice, linea ->
                println("${indice + 1}: $linea")
            }
        }

        // TODO 3.b: mostrar el código de salida y el tiempo.
        println("[código $codigo · $milisegundos ms]")

    } catch (e: Exception) {
        milisegundos = 0L
        codigo = -1
        println("Error al ejecutar la orden: ${e.message}")
        println("[código $codigo · $milisegundos ms]")
    } finally {
        // TODO 3.d: guardar el historial.
        File("historial.txt").appendText(
            "${orden.replace(';', ',')};$codigo;$milisegundos${System.lineSeparator()}"
        )

        salidaTemporal.delete()
    }
}

/**
 * Adapta la orden a la shell de Windows.
 */
private fun comandoShellWindows(orden: String): List<String> {
    val sistema = System.getProperty("os.name").lowercase()

    return if (sistema.contains("win")) {
        listOf("cmd.exe", "/c", orden)
    } else {
        listOf("/bin/sh", "-c", orden)
    }
}
