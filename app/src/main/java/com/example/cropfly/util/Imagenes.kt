package com.example.cropfly.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/** Ayudas para la foto del predio (cámara o galería). */
object Imagenes {
    private const val LADO_MAXIMO_PX = 1280
    private const val CALIDAD_JPEG = 80

    /** Archivo temporal donde la app de cámara va a dejar la foto recién tomada. */
    fun crearArchivoTemporal(contexto: Context): File {
        val carpeta = File(contexto.cacheDir, "fotos").apply { mkdirs() }
        return File.createTempFile("predio_", ".jpg", carpeta)
    }

    /** Dirección segura (FileProvider) que se le entrega a la cámara para que escriba en el archivo. */
    fun uriParaCamara(contexto: Context, archivo: File): Uri =
        FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)

    /**
     * Lee la imagen elegida, la reduce a un máximo de 1280 px y la guarda como JPG en la carpeta privada de la app.
     * Así pesa bastante menos de 2 MB y sigue disponible aunque la galería cambie. Devuelve la ruta, o null si falla.
     */
    fun guardarReducida(contexto: Context, origen: Uri): String? {
        return try {
            val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contexto.contentResolver.openInputStream(origen)?.use { BitmapFactory.decodeStream(it, null, medidas) }

            var reduccion = 1
            while (max(medidas.outWidth, medidas.outHeight) / reduccion > LADO_MAXIMO_PX) {
                reduccion *= 2
            }
            val opciones = BitmapFactory.Options().apply { inSampleSize = reduccion }
            val imagen = contexto.contentResolver.openInputStream(origen)?.use {
                BitmapFactory.decodeStream(it, null, opciones)
            } ?: return null

            val carpeta = File(contexto.filesDir, "fotos").apply { mkdirs() }
            val destino = File(carpeta, "predio_${System.currentTimeMillis()}.jpg")
            FileOutputStream(destino).use { imagen.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, it) }
            destino.absolutePath
        } catch (error: Exception) {
            null
        }
    }

    /** Versión pequeña de la foto para mostrarla en el formulario sin gastar memoria. */
    fun cargarMiniatura(ruta: String): Bitmap? {
        val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
        return BitmapFactory.decodeFile(ruta, opciones)
    }
}
