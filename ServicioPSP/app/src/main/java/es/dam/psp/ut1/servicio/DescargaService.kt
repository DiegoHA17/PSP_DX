package es.dam.psp.ut1.servicio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
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

    @Volatile
    private var hiloDescarga: Thread? = null

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

        if (hiloDescarga?.isAlive == true) {
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
        try {
            for (progreso in 0..100 step 10) {
                registrar("Descargando $progreso %")
                actualizarNotificacion(progreso)

                if (progreso < 100) {
                    Thread.sleep(PAUSA_MS)
                }
            }

            registrar("Descarga completada")
            // Sale de primer plano pero deja visible la notificación final.
            stopForeground(STOP_FOREGROUND_DETACH)
        } catch (e: InterruptedException) {
            registrar("Descarga cancelada")
            // Por si se llegó a publicar una actualización tras onDestroy.
            gestorNotificaciones().cancel(NOTIFICATION_ID)
        } finally {
            hiloDescarga = null
            // stopSelf() sin startId: con stopSelf(startId) el servicio no se
            // paraba si se había pulsado Iniciar más de una vez.
            stopSelf()
        }
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
