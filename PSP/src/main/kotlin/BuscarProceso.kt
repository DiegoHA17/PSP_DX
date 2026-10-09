
package es.dam.psp.ut1.practicas

import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    // Exige una cadena de búsqueda.
    val filtro = args.firstOrNull()?.trim()

    if (filtro.isNullOrEmpty()) {
        println("Uso: BuscarProcesos <cadena>")
        return
    }

    println("PID | PPID | USUARIO | INICIO | CPU | COMANDO")

    // Recorre los procesos que la JVM puede consultar.
    ProcessHandle.allProcesses().forEach { proceso ->
        val info = proceso.info()

        // Usa la línea de comandos y, si falta, el nombre ejecutable.
        val comando = info.commandLine()
            .orElseGet { info.command().orElse("") }

        if (comando.contains(filtro, ignoreCase = true)) {
            val pid = proceso.pid()
            val ppid = proceso.parent()
                .map { it.pid().toString() }
                .orElse("-")

            val usuario = info.user().orElse("-")

            val inicio = info.startInstant()
                .map {
                    DateTimeFormatter.ISO_INSTANT.format(it)
                }
                .orElse("-")

            val cpu = info.totalCpuDuration()
                .map { it.toString() }
                .orElse("-")

            println("$pid | $ppid | $usuario | $inicio | $cpu | $comando")
        }
    }
}

