package es.dam.psp.ut2.guiada

/**
 * Práctica guiada · Reto B. Versión corregida de TaquillaIA (misma carpeta, guiada/).
 *
 * Apuntes UT-2, apartados 5 (condición de carrera) y 6.1 (synchronized, atómicas y cerrojos).
 * No se ejecuta sola: cambia la línea `fabrica` de Verificador.kt a ::TaquillaSegura y ejecuta
 * Verificador.kt. Debe cumplir el contrato de Taquilla.kt.
 *
 * Todo el estado (duenos, vendidas y caja) se protege con un único cerrojo. Invariante que se
 * mantiene siempre que se suelta el cerrojo:
 *   vendidas == asientos con dueño   y   caja == vendidas * PRECIO
 * Por eso basta mirar `vendidas` para saber si quedan n libres antes de buscarlos.
 */
class TaquillaSegura(override val aforo: Int) : Taquilla {

    private val cerrojo = Any()

    // Solo se leen o escriben dentro de synchronized(cerrojo): no hacen falta atómicas ni @Volatile.
    private val duenos = arrayOfNulls<String>(aforo)   // null = asiento libre
    private var vendidas = 0
    private var caja = 0L

    override fun comprar(cliente: String, n: Int): List<Int> {
        if (n <= 0) return emptyList()
        synchronized(cerrojo) {
            // Comprobar y actuar dentro del mismo cerrojo: nadie puede ocupar asientos en medio.
            if (aforo - vendidas < n) return emptyList()       // todo o nada

            val asignados = ArrayList<Int>(n)
            var asiento = 0
            while (asignados.size < n) {                        // hay al menos n libres: siempre termina
                if (duenos[asiento] == null) {
                    duenos[asiento] = cliente
                    asignados += asiento
                }
                asiento++
            }
            vendidas += n
            caja += n * PRECIO
            return asignados
        }
    }

    // También con el cerrojo: así se ve el último valor escrito (visibilidad) y nunca un estado a medias.
    override fun vendidas(): Int = synchronized(cerrojo) { vendidas }

    override fun recaudado(): Long = synchronized(cerrojo) { caja }
}
