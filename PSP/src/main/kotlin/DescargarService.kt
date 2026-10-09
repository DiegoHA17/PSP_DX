package es.dam.psp.ut1.practicas

class DescargaService {

    @Volatile
    private var hiloDescarga: Thread? = null

    @Volatile
    private var cancelada = false

    /**
     * Inicia la descarga simulada en un hilo independiente.
     */
    @Synchronized
    fun iniciar() {
        if (hiloDescarga?.isAlive == true) {
            println("Ya hay una descarga en curso.")
            return
        }

        cancelada = false

        hiloDescarga = Thread {
            try {
                println("Descarga iniciada.")

                for (progreso in 0..100 step 10) {
                    if (Thread.currentThread().isInterrupted || cancelada) {
                        println("Descarga cancelada.")
                        return@Thread
                    }

                    println("Progreso de la descarga: $progreso %")

                    if (progreso < 100) {
                        Thread.sleep(500)
                    }
                }

                println("Descarga completada.")
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                println("Descarga interrumpida.")
            } finally {
                hiloDescarga = null
            }
        }.apply {
            name = "HiloDescarga"
            start()
        }
    }

    /**
     * Cancela la descarga en curso.
     */
    @Synchronized
    fun cancelar() {
        cancelada = true
        hiloDescarga?.interrupt()
        println("Solicitud de cancelación enviada.")
    }

    /**
     * Espera a que termine el hilo de descarga.
     */
    fun esperarFin() {
        hiloDescarga?.join()
    }
}

fun main() {
    val servicio = DescargaService()

    servicio.iniciar()

    // Ejemplo: esperar a que la simulación termine.
    servicio.esperarFin()
}
