package es.dam.psp.ut2.guiada

/**
 * Práctica guiada · Reto C. Taquilla con devoluciones y lista de espera.
 *
 * Apuntes UT-2, apartados 2 (interrupt), 6.1 (synchronized) y 7.1 (wait() y notifyAll()).
 * No se ejecuta sola: la prueba VerificadorEspera.kt (misma carpeta, guiada/).
 * Debe cumplir el contrato de TaquillaConEspera (Taquilla.kt).
 *
 * Mismo esquema que TaquillaSegura: un único cerrojo protege duenos, vendidas y caja, con el
 * invariante vendidas == asientos con dueño y caja == vendidas * PRECIO. El cerrojo es también
 * el monitor de la lista de espera: se espera con cerrojo.wait() y se avisa con notifyAll().
 */
class TaquillaEspera(override val aforo: Int) : TaquillaConEspera {

    // Object (y no Any) para poder llamar a wait() y notifyAll() sobre él.
    private val cerrojo = Object()

    private val duenos = arrayOfNulls<String>(aforo)   // null = asiento libre
    private var vendidas = 0
    private var caja = 0L

    override fun comprar(cliente: String, n: Int): List<Int> {
        if (n <= 0) return emptyList()
        synchronized(cerrojo) {
            if (aforo - vendidas < n) return emptyList()       // todo o nada
            val asignados = ArrayList<Int>(n)
            var asiento = 0
            while (asignados.size < n) {
                if (duenos[asiento] == null) asignados += vender(asiento, cliente)
                asiento++
            }
            return asignados
        }
    }

    override fun vendidas(): Int = synchronized(cerrojo) { vendidas }

    override fun recaudado(): Long = synchronized(cerrojo) { caja }

    override fun devolver(cliente: String, asiento: Int): Boolean {
        synchronized(cerrojo) {
            if (asiento !in 0 until aforo || duenos[asiento] != cliente) return false
            duenos[asiento] = null
            vendidas--
            caja -= PRECIO
            // Despierta a todos los que esperan; solo uno se lleva el asiento y los demás,
            // al volver a comprobar el while, se vuelven a dormir.
            cerrojo.notifyAll()
            return true
        }
    }

    override fun esperarEntrada(cliente: String): Int? {
        synchronized(cerrojo) {
            try {
                // while y no if: al despertar otro hilo puede haberse llevado ya el asiento.
                while (vendidas == aforo) cerrojo.wait()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()            // se conserva la marca de interrupción
                return null
            }
            return vender(duenos.indexOfFirst { it == null }, cliente)
        }
    }

    /** Vende un asiento libre. Solo se llama con el cerrojo cogido. */
    private fun vender(asiento: Int, cliente: String): Int {
        duenos[asiento] = cliente
        vendidas++
        caja += PRECIO
        return asiento
    }
}
