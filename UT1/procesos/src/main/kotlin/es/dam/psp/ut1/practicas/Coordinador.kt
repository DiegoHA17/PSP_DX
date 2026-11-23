package es.dam.psp.ut1.practicas

import java.io.File
import java.io.IOException
import kotlin.system.measureTimeMillis

/** Resultado que devuelve un Contador para un fichero. */
data class Resultado(
    val fichero: String,
    val lineas: Long,
    val palabras: Long,
    val caracteres: Long,
    val palabraMasFrecuente: String
)

/** Resultados de un modo de ejecución y el tiempo que ha tardado. */
data class Ejecucion(
    val resultados: List<Resultado>,
    val milisegundos: Long
)

/**
 * 4.a: arranca un proceso Contador cuya entrada estándar es el fichero
 * (redirectInput) y que hereda la salida de error del padre.
 */
private fun lanzarContador(fichero: File): Process {
    require(fichero.isFile) {
        "No existe el fichero: ${fichero.absolutePath}"
    }

    val esWindows = System.getProperty("os.name")
        .lowercase()
        .contains("win")

    // Misma JVM y mismo classpath que el Coordinador.
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
 * 4.b: lee la línea del hijo, espera a que termine y construye el Resultado.
 * Si el código de salida no es 0 lanza una excepción con un mensaje claro.
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

/** 4.c: procesa los ficheros de uno en uno. */
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

/** 4.d: lanza todos los hijos y después recoge los resultados. */
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

/**
 * Mejora opcional: nunca hay más de [maximo] hijos vivos a la vez.
 * Cuando se recoge un hijo se lanza el siguiente fichero pendiente.
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
        val pendientes = ficheros.iterator()
        val activos = ArrayDeque<Pair<File, Process>>()

        while (pendientes.hasNext() || activos.isNotEmpty()) {
            // Rellenar hasta el máximo permitido.
            while (pendientes.hasNext() && activos.size < maximo) {
                val fichero = pendientes.next()
                activos.addLast(fichero to lanzarContador(fichero))
            }

            // Recoger el hijo más antiguo para liberar una plaza.
            val (fichero, hijo) = activos.removeFirst()
            resultados.add(recogerResultado(fichero, hijo))
        }
    }

    return Ejecucion(resultados, tiempo)
}

/** 4.e: tabla con el resultado de cada fichero y una fila TOTAL. */
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

/** Compara dos ejecuciones fichero a fichero (Resultado es data class). */
private fun mismosResultados(a: Ejecucion, b: Ejecucion): Boolean =
    a.resultados.sortedBy { it.fichero } == b.resultados.sortedBy { it.fichero }

/** Aceleración = tiempo secuencial / tiempo concurrente. */
private fun aceleracion(secuencialMs: Long, concurrenteMs: Long): String =
    if (concurrenteMs > 0) {
        "%.2f".format(secuencialMs.toDouble() / concurrenteMs)
    } else {
        "N/D"
    }

/**
 * Uso: sin argumentos ejecuta los modos secuencial y concurrente.
 * Con el argumento "limitado" mide además concurrenteLimitado con 1, 2, 4 y 8.
 */
fun main(args: Array<String>) {
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

    val nucleos = Runtime.getRuntime().availableProcessors()
    println("Ficheros encontrados: ${ficheros.size}")
    println("Procesadores lógicos detectados: $nucleos")

    val secuencial = secuencial(ficheros)
    println("\n=== RESULTADOS SECUENCIALES ===")
    imprimirTabla(secuencial.resultados)

    val concurrente = concurrente(ficheros)
    println("\n=== RESULTADOS CONCURRENTES ===")
    imprimirTabla(concurrente.resultados)

    println("\n=== COMPROBACIÓN ===")
    if (mismosResultados(secuencial, concurrente)) {
        println("CORRECTO: ambos modos producen los mismos resultados.")
    } else {
        System.err.println("ERROR: los resultados secuenciales y concurrentes no coinciden.")
    }

    println("\n=== TIEMPOS ===")
    println("Tiempo secuencial: ${secuencial.milisegundos} ms")
    println("Tiempo concurrente: ${concurrente.milisegundos} ms")
    println(
        "Aceleración (speedup): " +
                aceleracion(secuencial.milisegundos, concurrente.milisegundos) +
                " (máximo teórico: $nucleos)"
    )

    if (args.firstOrNull() == "limitado") {
        println("\n=== MEJORA OPCIONAL: concurrenteLimitado ===")
        println("%-14s %12s %12s %12s".format("Máximo hijos", "Tiempo (ms)", "Aceleración", "Coinciden"))
        println("-".repeat(53))

        for (maximo in listOf(1, 2, 4, 8)) {
            val limitado = concurrenteLimitado(ficheros, maximo)
            println(
                "%-14d %12d %12s %12s".format(
                    maximo,
                    limitado.milisegundos,
                    aceleracion(secuencial.milisegundos, limitado.milisegundos),
                    if (mismosResultados(secuencial, limitado)) "sí" else "NO"
                )
            )
        }
    }
}
