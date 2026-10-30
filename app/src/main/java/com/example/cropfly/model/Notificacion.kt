package com.example.cropfly.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Aviso dentro de la app (la campanita de la barra superior). */
@Entity(tableName = "notificaciones")
data class Notificacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val mensaje: String,
    val fecha: Long = System.currentTimeMillis(),
    val leida: Boolean = false
)
