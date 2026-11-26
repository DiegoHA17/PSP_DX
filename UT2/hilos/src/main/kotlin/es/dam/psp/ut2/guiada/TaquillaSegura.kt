package es.dam.psp.ut2.guiada

/**
 * Práctica guiada · Reto B. Versión corregida de TaquillaIA (misma carpeta, guiada/).
 *
 * Apuntes UT-2, apartados 5 (condición de carrera) y 6.1 (synchronized, atómicas y cerrojos).
 * No se ejecuta sola: cambia la línea `fabrica` de Verificador.kt a ::TaquillaSegura y ejecuta
 * Verificador.kt. Debe cumplir el contrato de Taquilla.kt.
 */
class TaquillaSegura(override val aforo: Int) : Taquilla {

    private val cerrojo = Any()
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
}
