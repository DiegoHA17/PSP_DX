package es.dam.psp.ut1.practicas

import java.io.BufferedReader
import java.io.InputStreamReader

fun main() {
    val texto = BufferedReader(InputStreamReader(System.`in`))
        .use { it.readText() }

    // Contar líneas sin añadir una línea extra si el texto termina en salto.
    val lineas = if (texto.isEmpty()) {
        0L
    } else {
        val saltos = texto.count { it == '\n' }.toLong()
        saltos + if (texto.endsWith('\n')) 0L else 1L
    }

    // Contar palabras formadas por letras o números.
    val palabrasEncontradas = Regex("[\\p{L}\\p{N}]+")
        .findAll(texto)
        .map { it.value.lowercase() }
        .toList()

    val numeroPalabras = palabrasEncontradas.size.toLong()
    val caracteres = texto.length.toLong()

    // Buscar la palabra más frecuente.
    val frecuencias = palabrasEncontradas.groupingBy { it }.eachCount()
    val palabraMasFrecuente = frecuencias.entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key }
        )
        .firstOrNull()
        ?.key ?: ""

    // IMPORTANTE: esta debe ser la única línea escrita por el proceso.
    println("$lineas;$numeroPalabras;$caracteres;$palabraMasFrecuente")
}
