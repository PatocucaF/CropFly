package com.example.cropfly.util

import java.security.MessageDigest

/** Ayudas de seguridad básicas. */
object Seguridad {
    /** Convierte una contraseña en su hash SHA-256 (así nunca se guarda la contraseña real). */
    fun hash(clave: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(clave.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
