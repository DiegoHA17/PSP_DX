package es.dam.psp.ut2.guiada

/**
 * Práctica guiada · Reto C. Taquilla con devoluciones y lista de espera.
 *
 * Apuntes UT-2, apartados 2 (interrupt), 6.1 (synchronized) y 7.1 (wait() y notifyAll()).
 * No se ejecuta sola: la prueba VerificadorEspera.kt (misma carpeta, guiada/).
 * Debe cumplir el contrato de TaquillaConEspera (Taquilla.kt).
 */
class TaquillaEspera(override val aforo: Int) : TaquillaConEspera {

    private val cerrojo = Object()
    private val asientos = mutableMapOf<Int, String>()
    private var contador = 0
    private var caja = 0L

    // Bajo cerrojo, cada asiento tiene un solo dueño y, al terminar cada operación:
    // contador == asientos.size, 0 <= contador <= aforo y caja == contador * PRECIO.
    override fun comprar(cliente: String, n: Int): List<Int> = synchronized(cerrojo) {
        if (n <= 0 || n > aforo - contador) return@synchronized emptyList()

        val asignados = mutableListOf<Int>()
        for (asiento in 0 until aforo) {
            if (asignados.size == n) break
            if (!asientos.containsKey(asiento)) {
                asientos[asiento] = cliente
                asignados.add(asiento)
            }
        }

        contador += n
        caja += n * PRECIO
        asignados
    }

    override fun vendidas(): Int = synchronized(cerrojo) { contador }

    override fun recaudado(): Long = synchronized(cerrojo) { caja }

    override fun devolver(cliente: String, asiento: Int): Boolean = synchronized(cerrojo) {
        if (asientos[asiento] != cliente) return@synchronized false

        asientos.remove(asiento)
        contador--
        caja -= PRECIO
        cerrojo.notifyAll()
        true
    }

    override fun esperarEntrada(cliente: String): Int? = synchronized(cerrojo) {
        while (contador >= aforo) {
            try {
                cerrojo.wait()
            } catch (_: InterruptedException) {
                return@synchronized null
            }
        }

        // synchronized es reentrante: la compra conserva el mismo cerrojo.
        comprar(cliente, 1).first()
    }
}
