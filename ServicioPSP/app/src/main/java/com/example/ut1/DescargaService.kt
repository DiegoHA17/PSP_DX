package com.example.ut1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log

class DescargaService : Service() {

    companion object {
        private const val CHANNEL_ID = "canal_descarga"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "DescargaService"
    }

    @Volatile
    private var hiloDescarga: Thread? = null

    override fun onCreate() {
        super.onCreate()
        crearCanal()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        startForeground(
            NOTIFICATION_ID,
            crearNotificacion(0, "Preparando descarga...")
        )

        if (hiloDescarga?.isAlive == true) {
            return START_NOT_STICKY
        }

        hiloDescarga = Thread {
            try {
                for (progreso in 0..100 step 10) {
                    if (Thread.currentThread().isInterrupted) {
                        return@Thread
                    }

                    actualizarNotificacion(progreso)

                    if (progreso < 100) {
                        Thread.sleep(500)
                    }
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                Log.d(TAG, "Descarga cancelada")
            } finally {
                hiloDescarga = null
                stopSelf(startId)
            }
        }.apply {
            name = "HiloDescarga"
            start()
        }

        return START_NOT_STICKY
    }

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                "Descargas",
                NotificationManager.IMPORTANCE_LOW
            )

            val gestor = getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            gestor.createNotificationChannel(canal)
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

        val gestor = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        gestor.notify(
            NOTIFICATION_ID,
            crearNotificacion(progreso, mensaje)
        )
    }

    override fun onDestroy() {
        hiloDescarga?.interrupt()
        hiloDescarga = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
