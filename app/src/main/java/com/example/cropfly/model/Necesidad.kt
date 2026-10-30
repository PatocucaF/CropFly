package com.example.cropfly.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Necesidad de servicio registrada por un productor.
 * La ubicación es referencial: una zona de la lista o la ubicación simulada (latitud/longitud ficticias).
 */
@Entity(tableName = "necesidades")
data class Necesidad(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productorId: Long,
    val servicio: String,
    val cultivo: String,
    val superficieHa: Int,
    val zona: String,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val desde: Long,
    val hasta: Long,
    val producto: String = "",
    val notas: String = "",
    val fotoPath: String? = null,
    val esEjemplo: Boolean = false,
    val creadaEn: Long = System.currentTimeMillis()
)
