
package es.dam.psp.ut1.ejemplos

fun main(args: Array<String>) {
    // Lee los segundos recibidos como argumento.
    val segundos = args.firstOrNull()?.toLongOrNull() ?: 300L

    println("Dormilon iniciado. PID: ${ProcessHandle.current().pid()}")
    println("Duración: $segundos segundos")

    // Mantiene vivo el proceso para poder localizarlo.
    Thread.sleep(segundos * 1_000)

    println("Dormilon finalizado")
}
