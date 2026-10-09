package es.dam.psp.ut1.practicas

import java.io.File
import java.io.IOException
import kotlin.system.measureTimeMillis



/**
 * Inicia un proceso Contador cuya entrada estándar es el fichero.
 */
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

/**
 * Lee la salida del proceso hijo, espera a que termine
 * y construye el objeto Resultado.
 */
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
                    "El proceso Contador terminó con el código $codigoSalida."
        )
    }

    if (linea == null) {
        throw IOException(
            "El proceso Contador no devolvió resultados para '${fichero.name}'."
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
            "Los datos numéricos devueltos para '${fichero.name}' " +
                    "no son válidos: $linea",
            e
        )
    }
}

/**
 * Procesa los ficheros de uno en uno.
 * Cada proceso termina antes de iniciar el siguiente.
 */
private fun secuencial(ficheros: List<File>): Ejecucion {
    val resultados = mutableListOf<Resultado>()

    val tiempo = measureTimeMillis {
        for (fichero in ficheros) {
            val hijo = lanzarContador(fichero)
            val resultado = recogerResultado(fichero, hijo)
            resultados.add(resultado)
        }
    }

    return Ejecucion(
        resultados = resultados,
        milisegundos = tiempo
    )
}

/**
 * Inicia todos los procesos antes de recoger sus resultados.
 */
private fun concurrente(ficheros: List<File>): Ejecucion {
    val resultados = mutableListOf<Resultado>()

    val tiempo = measureTimeMillis {
        val procesos = ficheros.map { fichero ->
            fichero to lanzarContador(fichero)
        }

        for ((fichero, hijo) in procesos) {
            val resultado = recogerResultado(fichero, hijo)
            resultados.add(resultado)
        }
    }

    return Ejecucion(
        resultados = resultados,
        milisegundos = tiempo
    )
}

/**
 * Imprime los resultados y los totales.
 */
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

    // 1. Ejecución secuencial.
    val secuencial = secuencial(ficheros)

    println("\n=== Resultados secuenciales ===")
    imprimirTabla(secuencial.resultados)

    // 2. Ejecución concurrente.
    val concurrente = concurrente(ficheros)

    println("\n=== Resultados concurrentes ===")
    imprimirTabla(concurrente.resultados)

    // 3. Comparar los resultados de ambas ejecuciones.
    val resultadosSecuenciales =
        secuencial.resultados.sortedBy { it.fichero }

    val resultadosConcurrentes =
        concurrente.resultados.sortedBy { it.fichero }

    val equivalentes =
        resultadosSecuenciales == resultadosConcurrentes

    println("\n=== Comparación ===")
    println("Resultados equivalentes: $equivalentes")
    println("Tiempo secuencial: ${secuencial.milisegundos} ms")
    println("Tiempo concurrente: ${concurrente.milisegundos} ms")

    if (concurrente.milisegundos > 0) {
        val aceleracion =
            secuencial.milisegundos.toDouble() /
                    concurrente.milisegundos

        println("Aceleración (speedup): %.2f".format(aceleracion))
    } else {
        println("Aceleración no calculable: tiempo concurrente de 0 ms.")
    }

    if (equivalentes) {
        println("Correcto: los resultados de ambas ejecuciones coinciden.")
    } else {
        System.err.println(
            "ERROR: los resultados secuenciales y concurrentes no coinciden."
        )
    }
}
