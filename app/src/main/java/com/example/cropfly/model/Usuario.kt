package com.example.cropfly.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cuenta de la app (todo ficticio).
 * - sesionActiva: es la "sesión guardada". Si está en true, al abrir la app se entra directo.
 * - claveHash: nunca se guarda la contraseña, solo su hash SHA-256.
 */
@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val claveHash: String,
    val rol: String,
    val activo: Boolean = true,
    val sesionActiva: Boolean = false,
    val ubicacionPermitida: Boolean = false
)
