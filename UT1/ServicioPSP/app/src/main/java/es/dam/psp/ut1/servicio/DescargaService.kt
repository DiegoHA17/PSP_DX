package es.dam.psp.ut1.servicio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.util.Log

/**
 * Servicio en primer plano que simula una descarga en un hilo aparte
 * y muestra el progreso en una notificación.
 */
class DescargaService : Service() {

    companion object {
        private const val CHANNEL_ID = "canal_descarga"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "PSP-UT1"
        private const val PAUSA_MS = 500L
    }

    /** Hilo de la descarga en curso. Solo se lee y modifica en el hilo main. */
    private var hiloDescarga: Thread? = null

    private val hiloMain = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        registrar("onCreate")
        crearCanal()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        registrar("onStartCommand")

        // 5.b: pasar a primer plano (tipo dataSync declarado en el manifiesto).
        startForeground(
            NOTIFICATION_ID,
            crearNotificacion(0, "Preparando descarga...")
        )

        // Sigue habiendo descarga hasta que terminarDescarga la cierra.
        if (hiloDescarga != null) {
            registrar("Ya hay una descarga en curso")
            return START_NOT_STICKY
        }

        // 5.b: la descarga se simula en un hilo aparte, no en el hilo main.
        hiloDescarga = Thread { descargar() }.apply {
            name = "HiloDescarga"
            start()
        }

        return START_NOT_STICKY
    }

    /** Bucle de la descarga simulada; se ejecuta en el hilo HiloDescarga. */
    private fun descargar() {
        val hilo = Thread.currentThread()
        var completada = false

        try {
            for (progreso in 0..100 step 10) {
                registrar("Descargando $progreso %")
                actualizarNotificacion(progreso)

                if (progreso < 100) {
                    Thread.sleep(PAUSA_MS)
                }
            }

            completada = true
            registrar("Descarga completada")
        } catch (e: InterruptedException) {
            registrar("Descarga cancelada")
        } finally {
            // El cierre se hace en el hilo main, el mismo de onStartCommand y
            // onDestroy, para que no pueda intercalarse con ellos.
            hiloMain.post { terminarDescarga(hilo, completada) }
        }
    }

    /** Se ejecuta en el hilo main cuando termina el hilo de descarga. */
    private fun terminarDescarga(hilo: Thread, completada: Boolean) {
        if (hiloDescarga !== hilo) {
            // onDestroy ya canceló esta descarga: se retira cualquier
            // notificación que el hilo publicara después.
            gestorNotificaciones().cancel(NOTIFICATION_ID)
            return
        }

        hiloDescarga = null

        if (completada) {
            // Sale de primer plano pero deja visible la notificación final.
            stopForeground(STOP_FOREGROUND_DETACH)
        }

        // stopSelf() sin startId: con stopSelf(startId) el servicio no se
        // paraba si se había pulsado Iniciar más de una vez.
        stopSelf()
    }

    /** Escribe en Logcat (tag PSP-UT1) el PID, el hilo y el evento. */
    private fun registrar(evento: String) {
        Log.d(
            TAG,
            "Servicio · $evento · PID=${Process.myPid()} · hilo=${Thread.currentThread().name}"
        )
    }

    private fun gestorNotificaciones(): NotificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                "Descargas",
                NotificationManager.IMPORTANCE_LOW
            )

            gestorNotificaciones().createNotificationChannel(canal)
        }
    }

    private fun crearNotificacion(
        progreso: Int,
        mensaje: String
    ): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Descarga")
            .setContentText(mensaje)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progreso, false)
            .setOngoing(progreso < 100)
            .build()
    }

    private fun actualizarNotificacion(progreso: Int) {
        val mensaje = if (progreso == 100) {
            "Descarga completada"
        } else {
            "Descargando... $progreso %"
        }

        gestorNotificaciones().notify(
            NOTIFICATION_ID,
            crearNotificacion(progreso, mensaje)
        )
    }

    // 5.b: al destruir el servicio se cancela la descarga.
    override fun onDestroy() {
        registrar("onDestroy")
        hiloDescarga?.interrupt()
        hiloDescarga = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
