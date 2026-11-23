package es.dam.psp.ut1

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import es.dam.psp.ut1.servicio.DescargaService

class MainActivity : Activity() {

    private lateinit var textoPid: TextView

    companion object {
        private const val SOLICITUD_NOTIFICACIONES = 100
        private const val TAG = "PSP-UT1"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mismo formato que el servicio para comparar PID e hilo en Logcat.
        Log.d(TAG, "Actividad · onCreate · PID=${Process.myPid()} · hilo=${Thread.currentThread().name}")

        textoPid = TextView(this).apply {
            text = "PID de la actividad: ${Process.myPid()}"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(16, 24, 16, 24)
        }

        val botonIniciar = Button(this).apply {
            text = "Iniciar"
            setOnClickListener {
                iniciarDescarga()
            }
        }

        val botonParar = Button(this).apply {
            text = "Parar"
            setOnClickListener {
                pararDescarga()
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)

            addView(
                textoPid,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            addView(
                botonIniciar,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            addView(
                botonParar,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        setContentView(layout)

        solicitarPermisoNotificaciones()
    }

    private fun solicitarPermisoNotificaciones() {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                SOLICITUD_NOTIFICACIONES
            )
        }
    }

    private fun iniciarDescarga() {
        val intent = Intent(this, DescargaService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        Toast.makeText(
            this,
            "Servicio iniciado",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun pararDescarga() {
        stopService(Intent(this, DescargaService::class.java))

        Toast.makeText(
            this,
            "Solicitud de parada enviada",
            Toast.LENGTH_SHORT
        ).show()
    }
}
