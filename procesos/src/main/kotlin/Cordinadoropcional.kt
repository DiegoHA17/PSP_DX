package es.dam.psp.ut1.practicas

import java.io.File
import java.io.IOException
import kotlin.system.measureTimeMillis



private data class MedicionLimitada(
    val maximo: Int,
    val milisegundos: Long,
    val aceleracion: Double
)

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

    val codigo = hijo.waitFor()

    if (codigo != 0) {
        throw IOException(
            "Error al procesar '${fichero.name}'. Código de salida: $codigo"
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
            "Resultado no válido para '${fichero.name}': $linea"
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
            "Datos numéricos no válidos para '${fichero.name}': $linea",
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
        val procesos = ficheros.map { fichero ->
            fichero to lanzarContador(fichero)
        }

        for ((fichero, hijo) in procesos) {
            resultados.add(recogerResultado(fichero, hijo))
        }
    }

    return Ejecucion(resultados, tiempo)
}

/**
 * Procesa los ficheros manteniendo como máximo 'maximo'
 * procesos hijos vivos al mismo tiempo.
 */
private fun concurrenteLimitado(
    ficheros: List<File>,
    maximo: Int
): Ejecucion {
    require(maximo > 0) {
        "El máximo de procesos debe ser mayor que cero."
    }

    val resultados = mutableListOf<Resultado>()

    val tiempo = measureTimeMillis {
        // Cola de ficheros pendientes.
        val pendientes = ficheros.iterator()

        // Procesos actualmente en ejecución, asociados a su fichero.
        val activos = mutableListOf<Pair<File, Process>>()

        // Lanzar inicialmente hasta 'maximo' procesos.
        while (pendientes.hasNext() && activos.size < maximo) {
            val fichero = pendientes.next()
            activos.add(fichero to lanzarContador(fichero))
        }

        // Mientras haya procesos activos, recoger uno y lanzar otro.
        while (activos.isNotEmpty()) {
            // Esperar al primer proceso de la lista.
            val (fichero, hijo) = activos.removeAt(0)
            resultados.add(recogerResultado(fichero, hijo))

            // Al liberar una plaza, lanzar el siguiente fichero pendiente.
            if (pendientes.hasNext()) {
                val siguiente = pendientes.next()
                activos.add(siguiente to lanzarContador(siguiente))
            }
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

    for (r in resultados) {
        println(
            "%-22s %10d %10d %12d %25s".format(
                r.fichero,
                r.lineas,
                r.palabras,
                r.caracteres,
                r.palabraMasFrecuente
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
    println(
        "Procesadores lógicos detectados: " +
                Runtime.getRuntime().availableProcessors()
    )

    // Ejecución secuencial de referencia.
    val referencia = secuencial(ficheros)

    println("\n=== RESULTADOS SECUENCIALES ===")
    imprimirTabla(referencia.resultados)
    println("Tiempo secuencial: ${referencia.milisegundos} ms")

    // Ejecución concurrente sin límite explícito.
    val sinLimite = concurrente(ficheros)

    println("\n=== RESULTADOS CONCURRENTES SIN LÍMITE ===")
    imprimirTabla(sinLimite.resultados)
    println("Tiempo concurrente: ${sinLimite.milisegundos} ms")

    val equivalentesSinLimite =
        referencia.resultados.sortedBy { it.fichero } ==
                sinLimite.resultados.sortedBy { it.fichero }

    println("Resultados equivalentes: $equivalentesSinLimite")

    // Medir los límites solicitados.
    val mediciones = mutableListOf<MedicionLimitada>()

    for (maximo in listOf(1, 2, 4, 8)) {
        println("\nEjecutando con un máximo de $maximo procesos...")

        val ejecucion = concurrenteLimitado(ficheros, maximo)

        val equivalentes =
            referencia.resultados.sortedBy { it.fichero } ==
                    ejecucion.resultados.sortedBy { it.fichero }

        if (!equivalentes) {
            throw IllegalStateException(
                "Los resultados no coinciden con límite $maximo."
            )
        }

        val aceleracion =
            if (ejecucion.milisegundos > 0) {
                referencia.milisegundos.toDouble() /
                        ejecucion.milisegundos
            } else {
                Double.NaN
            }

        mediciones.add(
            MedicionLimitada(
                maximo = maximo,
                milisegundos = ejecucion.milisegundos,
                aceleracion = aceleracion
            )
        )

        println("Resultados equivalentes: $equivalentes")
    }

    // Tabla resumen de los límites.
    println("\n=== COMPARACIÓN DE LÍMITES ===")
    println(
        "%-15s %18s %18s".format(
            "Máximo hijos",
            "Tiempo (ms)",
            "Aceleración"
        )
    )
    println("-".repeat(53))

    for (m in mediciones) {
        println(
            "%-15d %18d %18s".format(
                m.maximo,
                m.milisegundos,
                if (m.aceleracion.isNaN()) {
                    "N/D"
                } else {
                    "%.2f".format(m.aceleracion)
                }
            )
        )
    }

    println("\nTodas las ejecuciones limitadas han producido los mismos resultados.")
}
