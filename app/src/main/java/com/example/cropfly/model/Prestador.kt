package com.example.cropfly.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Perfil demostrativo de un prestador de servicios agrícolas.
 * usuarioId es null cuando el prestador es parte del catálogo de prueba (no tiene cuenta).
 * Las fechas se guardan como milisegundos UTC (ver Fechas.kt).
 */
@Entity(tableName = "prestadores")
data class Prestador(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long? = null,
    val nombre: String,
    val servicios: List<String> = emptyList(),
    val cobertura: List<String> = emptyList(),
    val cultivos: List<String> = emptyList(),
    val capacidadHaDia: Int = 0,
    val experienciaAnios: Int = 0,
    val calificacion: Double = 0.0,
    val disponibleDesde: Long = 0L,
    val disponibleHasta: Long = 0L,
    val activo: Boolean = true
) {
    /** Un perfil se ve en el matching solo si ya ofrece al menos un servicio. */
    val perfilCompleto: Boolean
        get() = servicios.isNotEmpty() && cobertura.isNotEmpty()

    /** Iniciales para el cuadro verde (ej: "Drones Valle Sur" -> "DV"). */
    val iniciales: String
        get() = nombre.trim().split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
}
