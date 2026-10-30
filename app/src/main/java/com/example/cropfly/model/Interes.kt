package com.example.cropfly.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * "Me interesa": vinculación simulada entre una necesidad y un prestador.
 * Si se borra la necesidad o el prestador, sus intereses se borran solos (CASCADE).
 */
@Entity(
    tableName = "intereses",
    foreignKeys = [
        ForeignKey(
            entity = Necesidad::class,
            parentColumns = ["id"],
            childColumns = ["necesidadId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Prestador::class,
            parentColumns = ["id"],
            childColumns = ["prestadorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["necesidadId", "prestadorId"], unique = true),
        Index(value = ["prestadorId"])
    ]
)
data class Interes(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val necesidadId: Long,
    val prestadorId: Long,
    val productorId: Long,
    val enviadoEn: Long = System.currentTimeMillis(),
    val vista: Boolean = false
)

/** Un interés junto con la necesidad que lo originó (lo que ve el prestador en "Solicitudes"). */
data class SolicitudRecibida(
    @Embedded val interes: Interes,
    @Relation(parentColumn = "necesidadId", entityColumn = "id")
    val necesidad: Necesidad
)
