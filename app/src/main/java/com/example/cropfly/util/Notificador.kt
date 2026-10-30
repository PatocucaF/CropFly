package com.example.cropfly.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.cropfly.R

/** Recurso nativo: notificaciones locales del sistema Android (las que aparecen en la barra superior del teléfono). */
object Notificador {
    private const val CANAL_ID = "avisos_agroenlace"

    /** Android 13 en adelante exige que el usuario dé permiso; antes de eso no hace falta. */
    fun permisoConcedido(contexto: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun mostrar(contexto: Context, titulo: String, mensaje: String) {
        if (!permisoConcedido(contexto)) return
        crearCanal(contexto)
        val notificacion = NotificationCompat.Builder(contexto, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(contexto).notify(System.currentTimeMillis().toInt(), notificacion)
    }

    /** Desde Android 8 toda notificación necesita un "canal" (una categoría que el usuario puede silenciar). */
    private fun crearCanal(contexto: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(CANAL_ID, "Avisos de AgroEnlace", NotificationManager.IMPORTANCE_DEFAULT)
            val administrador = contexto.getSystemService(NotificationManager::class.java)
            administrador.createNotificationChannel(canal)
        }
    }
}
