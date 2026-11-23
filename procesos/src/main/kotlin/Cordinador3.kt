package es.dam.psp.ut1.practicas

import java.io.File
import java.io.IOException
import kotlin.system.measureTimeMillis


private fun lanzarContador(fichero: File): Process {
    require(fichero.isFile) {
        "No existe el fichero: ${fichero.absolutePath}"
    }

    val esWindows = System.getProperty("os.name")
        .lowercase()
        .contains("win")

    val java = File(
        System.getProperty("java.home"),
        "bin${File.separator}java${if (esWindows) ".exe" else ""}"
    ).absolutePath

    val classpath = System.getProperty("java.class.path")

    return ProcessBuilder(
        java,
        "-cp",
        classpath,
        "es.dam.psp.ut1.practicas.ContadorKt"
    )
        .redirectInput(fichero)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
}

private fun recogerResultado(
    fichero: File,
    hijo: Process
): Resultado {
    val linea = hijo.inputStream
        .bufferedReader()
        .use { it.readLine() }

    val codigoSalida = hijo.waitFor()

    if (codigoSalida != 0) {
        throw IOException(
            "Error al procesar '${fichero.name}'. " +
                    "Contador terminó con el código $codigoSalida."
        )
    }

    if (linea == null) {
        throw IOException(
            "Contador no devolvió resultados para '${fichero.name}'."
        )
    }

    val campos = linea.split(";")

    if (campos.size != 4) {
        throw IOException(
            "Formato de resultado no válido para '${fichero.name}': $linea"
        )
    }

    return try {
        Resultado(
            fichero = fichero.name,
            lineas = campos[0].toLong(),
            palabras = campos[1].toLong(),
            caracteres = campos[2].toLong(),
            palabraMasFrecuente = campos[3]
        )
    } catch (e: NumberFormatException) {
        throw IOException(
            "Los números devueltos para '${fichero.name}' no son válidos: $linea",
            e
        )
    }
}

private fun secuencial(ficheros: List<File>): Ejecucion {
    val resultados = mutableListOf<Resultado>()

    val tiempo = measureTimeMillis {
        for (fichero in ficheros) {
            val hijo = lanzarContador(fichero)
            resultados.add(recogerResultado(fichero, hijo))
        }
    }

    return Ejecucion(resultados, tiempo)
}

/**
 * Lanza todos los procesos hijos y después recoge sus resultados.
 */
private fun concurrente(ficheros: List<File>): Ejecucion {
    val resultados = mutableListOf<Resultado>()

    val tiempo = measureTimeMillis {
        // Primera fase: arrancar todos los procesos hijos.
        val procesos = ficheros.map { fichero ->
            fichero to lanzarContador(fichero)
        }

        // Segunda fase: recoger los resultados de los procesos ya lanzados.
        for ((fichero, hijo) in procesos) {
            resultados.add(recogerResultado(fichero, hijo))
        }
    }

    return Ejecucion(
        resultados = resultados,
        milisegundos = tiempo
    )
}

private fun imprimirTabla(resultados: List<Resultado>) {
    println()
    println(
        "%-22s %10s %10s %12s %25s".format(
            "Fichero",
            "Líneas",
            "Palabras",
            "Caracteres",
            "Palabra frecuente"
        )
    )
    println("-".repeat(85))

    for (resultado in resultados) {
        println(
            "%-22s %10d %10d %12d %25s".format(
                resultado.fichero,
                resultado.lineas,
                resultado.palabras,
                resultado.caracteres,
                resultado.palabraMasFrecuente
            )
        )
    }

    println("-".repeat(85))
    println(
        "%-22s %10d %10d %12d".format(
            "TOTAL",
            resultados.sumOf { it.lineas },
            resultados.sumOf { it.palabras },
            resultados.sumOf { it.caracteres }
        )
    )
}

fun main() {
    val directorio = File("datos")

    if (!directorio.isDirectory) {
        System.err.println(
            "No existe la carpeta datos/. Ejecuta primero GeneradorDatos.kt."
        )
        return
    }

    val ficheros = directorio.listFiles()
        ?.filter { it.isFile }
        ?.sortedBy { it.name }
        ?: emptyList()

    if (ficheros.isEmpty()) {
        System.err.println("No hay ficheros para procesar en datos/.")
        return
    }

    println("Ficheros encontrados: ${ficheros.size}")

    val ejecucionSecuencial = secuencial(ficheros)
    println("\n=== Resultados secuenciales ===")
    imprimirTabla(ejecucionSecuencial.resultados)

    val ejecucionConcurrente = concurrente(ficheros)
    println("\n=== Resultados concurrentes ===")
    imprimirTabla(ejecucionConcurrente.resultados)

    val resultadosSecuenciales =
        ejecucionSecuencial.resultados.sortedBy { it.fichero }

    val resultadosConcurrentes =
        ejecucionConcurrente.resultados.sortedBy { it.fichero }

    val equivalentes = resultadosSecuenciales == resultadosConcurrentes

    println("\n=== Comparación ===")
    println("Resultados equivalentes: $equivalentes")
    println("Tiempo secuencial: ${ejecucionSecuencial.milisegundos} ms")
    println("Tiempo concurrente: ${ejecucionConcurrente.milisegundos} ms")

    if (ejecucionConcurrente.milisegundos > 0) {
        val aceleracion =
            ejecucionSecuencial.milisegundos.toDouble() /
                    ejecucionConcurrente.milisegundos

        println("Aceleración (speedup): %.2f".format(aceleracion))
    } else {
        println("No se puede calcular la aceleración porque el tiempo concurrente es 0 ms.")
    }

    if (equivalentes) {
        println("Correcto: ambas ejecuciones producen los mismos resultados.")
    } else {
        System.err.println("ERROR: los resultados no coinciden.")
    }
}
