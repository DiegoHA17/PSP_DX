package es.dam.psp.ut1.practicas

import java.io.File
import kotlin.random.Random

fun main() {
    val directorio = File("datos")

    if (!directorio.exists() && !directorio.mkdirs()) {
        error("No se ha podido crear la carpeta datos/")
    }

    val numeroFicheros = 8
    val tamanoPorFichero = 4 * 1024 * 1024 // Aproximadamente 4 MiB
    val palabras = listOf(
        "Java", "Kotlin", "proceso", "hilo", "memoria",
        "sistema", "fichero", "entrada", "salida", "concurrencia",
        "programa", "ejecucion", "datos", "computador", "operacion"
    )

    val random = Random.Default

    repeat(numeroFicheros) { indice ->
        val fichero = File(directorio, "datos_${indice + 1}.txt")

        fichero.bufferedWriter().use { escritor ->
            var bytesEscritos = 0

            while (bytesEscritos < tamanoPorFichero) {
                val palabra = palabras[random.nextInt(palabras.size)]
                val linea = buildString {
                    repeat(8) {
                        if (it > 0) append(' ')
                        append(palabras[random.nextInt(palabras.size)])
                    }
                    append('\n')
                }

                escritor.write(linea)
                bytesEscritos += linea.toByteArray(Charsets.UTF_8).size
            }
        }

        println("Generado: ${fichero.path} (${fichero.length()} bytes)")
    }

    println("Generación terminada: $numeroFicheros ficheros en ${directorio.path}")
}
