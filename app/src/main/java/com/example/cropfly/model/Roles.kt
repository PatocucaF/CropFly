package com.example.cropfly.model

/** Los tres perfiles del caso TECMUNDO. Se guardan como texto en la base de datos. */
object Roles {
    const val PRODUCTOR = "productor"
    const val PRESTADOR = "prestador"
    const val ADMIN = "admin"

    /** Texto bonito para mostrar en pantalla (ej: la etiqueta verde de "Mi cuenta"). */
    fun etiqueta(rol: String): String = when (rol) {
        PRODUCTOR -> "Productor"
        PRESTADOR -> "Prestador"
        else -> "Administrador"
    }
}
