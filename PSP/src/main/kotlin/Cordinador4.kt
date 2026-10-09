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
            "Los valores numéricos de '${fichero.name}' no son válidos: $linea",
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

private fun concurrente(ficheros: List<File>): Ejecucion {
    val resultados = mutableListOf<Resultado>()

    val tiempo = measureTimeMillis {
        // Primero se lanzan todos los procesos hijos.
        val procesos = ficheros.map { fichero ->
            fichero to lanzarContador(fichero)
        }

        // Después se recogen los resultados.
        for ((fichero, hijo) in procesos) {
            resultados.add(recogerResultado(fichero, hijo))
        }
    }

    return Ejecucion(resultados, tiempo)
}

private fun imprimirTabla(resultados: List<Resultado>) {
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

    // Calcular los totales de líneas, palabras y caracteres.
    val totalLineas = resultados.sumOf { it.lineas }
    val totalPalabras = resultados.sumOf { it.palabras }
    val totalCaracteres = resultados.sumOf { it.caracteres }

    println("-".repeat(85))
    println(
        "%-22s %10d %10d %12d".format(
            "TOTAL",
            totalLineas,
            totalPalabras,
            totalCaracteres
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

    // Ejecutar el modo secuencial.
    val secuencial = secuencial(ficheros)

    println("\n=== RESULTADOS SECUENCIALES ===")
    imprimirTabla(secuencial.resultados)

    // Ejecutar el modo concurrente.
    val concurrente = concurrente(ficheros)

    println("\n=== RESULTADOS CONCURRENTES ===")
    imprimirTabla(concurrente.resultados)

    // Ordenar por nombre para comparar los mismos ficheros.
    val resultadosSecuenciales =
        secuencial.resultados.sortedBy { it.fichero }

    val resultadosConcurrentes =
        concurrente.resultados.sortedBy { it.fichero }

    // Resultado.equals compara todos los campos del data class.
    val equivalentes =
        resultadosSecuenciales == resultadosConcurrentes

    println("\n=== COMPROBACIÓN ===")
    if (equivalentes) {
        println("CORRECTO: ambos modos producen los mismos resultados.")
    } else {
        println("ERROR: los resultados secuenciales y concurrentes no coinciden.")
    }

    println("\n=== TIEMPOS ===")
    println("Tiempo secuencial: ${secuencial.milisegundos} ms")
    println("Tiempo concurrente: ${concurrente.milisegundos} ms")

    if (concurrente.milisegundos > 0) {
        val aceleracion =
            secuencial.milisegundos.toDouble() / concurrente.milisegundos

        println("Aceleración (speedup): %.2f".format(aceleracion))
    } else {
        println("Aceleración no calculable: tiempo concurrente de 0 ms.")
    }
}
