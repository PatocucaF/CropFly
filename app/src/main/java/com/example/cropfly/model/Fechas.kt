package com.example.cropfly.model

import java.util.Calendar
import java.util.TimeZone

/**
 * Ayudas para manejar fechas sin java.time (así funciona en Android 7 / minSdk 24).
 * Todas las fechas son milisegundos a medianoche UTC, igual que entrega el selector de fechas de Material 3.
 */
object Fechas {
    const val MILIS_POR_DIA = 86_400_000L

    private val utc: TimeZone = TimeZone.getTimeZone("UTC")
    private val meses = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

    /** Crea una fecha. Ejemplo: crear(2026, 10, 24) es el 24 de octubre de 2026. */
    fun crear(anio: Int, mes: Int, dia: Int): Long {
        val calendario = Calendar.getInstance(utc)
        calendario.clear()
        calendario.set(anio, mes - 1, dia)
        return calendario.timeInMillis
    }

    /** "24 oct" */
    fun corta(millis: Long): String {
        val calendario = Calendar.getInstance(utc)
        calendario.timeInMillis = millis
        return "${calendario.get(Calendar.DAY_OF_MONTH)} ${meses[calendario.get(Calendar.MONTH)]}"
    }

    /** "24 oct al 3 nov" */
    fun rango(desde: Long, hasta: Long): String = "${corta(desde)} al ${corta(hasta)}"

    /** "24/10/2026" (para mostrar en los campos de fecha). */
    fun completa(millis: Long): String {
        val calendario = Calendar.getInstance(utc)
        calendario.timeInMillis = millis
        val dia = calendario.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val mes = (calendario.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        return "$dia/$mes/${calendario.get(Calendar.YEAR)}"
    }

    /** Cantidad de días del rango contando el primero y el último. */
    fun diasEnRango(desde: Long, hasta: Long): Int = ((hasta - desde) / MILIS_POR_DIA).toInt() + 1

    /** Hoy, a medianoche UTC. */
    fun hoy(): Long {
        val ahora = Calendar.getInstance(utc)
        ahora.set(Calendar.HOUR_OF_DAY, 0)
        ahora.set(Calendar.MINUTE, 0)
        ahora.set(Calendar.SECOND, 0)
        ahora.set(Calendar.MILLISECOND, 0)
        return ahora.timeInMillis
    }
}
