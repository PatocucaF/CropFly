package com.example.cropfly.viewmodel

/**
 * Todas las reglas de validación de la app viven aquí, no en las pantallas.
 * Cada función recibe lo que escribió el usuario y devuelve el texto de error de cada campo
 * (o null si el campo está bien). Las pantallas solo muestran esos textos.
 */

data class ErroresAuth(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val repetirClave: String? = null,
    val terminos: String? = null,
    val general: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, correo, clave, repetirClave, terminos, general).any { it != null }
}

data class ErroresNecesidad(
    val servicio: String? = null,
    val cultivo: String? = null,
    val superficie: String? = null,
    val zona: String? = null,
    val desde: String? = null,
    val hasta: String? = null,
    val notas: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(servicio, cultivo, superficie, zona, desde, hasta, notas).any { it != null }
}

data class ErroresPerfil(
    val nombre: String? = null,
    val servicios: String? = null,
    val cobertura: String? = null,
    val cultivos: String? = null,
    val capacidad: String? = null,
    val experiencia: String? = null,
    val desde: String? = null,
    val hasta: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, servicios, cobertura, cultivos, capacidad, experiencia, desde, hasta).any { it != null }
}

data class ErroresNuevoPrestador(
    val nombre: String? = null,
    val zona: String? = null,
    val servicio: String? = null,
    val capacidad: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, zona, servicio, capacidad).any { it != null }
}

object Validaciones {
    const val SUPERFICIE_MAXIMA_HA = 100_000
    const val LARGO_MAXIMO_NOTAS = 200
    const val CAPACIDAD_MAXIMA_HA_DIA = 500
    const val EXPERIENCIA_MAXIMA_ANIOS = 60

    private val formatoCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun correoValido(correo: String): Boolean = formatoCorreo.matches(correo.trim())

    /** Mínimo 8 caracteres, con al menos una letra y un número. */
    fun claveSegura(clave: String): Boolean =
        clave.length >= 8 && clave.any { it.isLetter() } && clave.any { it.isDigit() }

    fun ingreso(correo: String, clave: String): ErroresAuth = ErroresAuth(
        correo = errorDeCorreo(correo, "Ingresa tu correo"),
        clave = if (clave.isEmpty()) "Ingresa tu contraseña" else null
    )

    fun registro(
        nombre: String,
        correo: String,
        clave: String,
        repetirClave: String,
        aceptaTerminos: Boolean
    ): ErroresAuth = ErroresAuth(
        nombre = when {
            nombre.isBlank() -> "Escribe un nombre o alias ficticio"
            nombre.trim().length < 3 -> "Usa al menos 3 caracteres"
            else -> null
        },
        correo = errorDeCorreo(correo, "Ingresa un correo"),
        clave = when {
            clave.isEmpty() -> "Crea una contraseña"
            !claveSegura(clave) -> "Mínimo 8, con letras y números"
            else -> null
        },
        repetirClave = when {
            repetirClave.isEmpty() -> "Repite la contraseña"
            repetirClave != clave -> "Las contraseñas no coinciden"
            else -> null
        },
        terminos = if (aceptaTerminos) null else "Debes aceptar para continuar"
    )

    fun necesidad(
        servicio: String,
        cultivo: String,
        superficie: String,
        zona: String,
        desde: Long?,
        hasta: Long?,
        notas: String
    ): ErroresNecesidad {
        val hectareas = superficie.trim().toIntOrNull()
        return ErroresNecesidad(
            servicio = if (servicio.isBlank()) "Elige el servicio que necesitas" else null,
            cultivo = if (cultivo.isBlank()) "Elige el tipo de cultivo" else null,
            superficie = when {
                superficie.isBlank() -> "Indica la superficie"
                hectareas == null || hectareas <= 0 -> "Ingresa un número mayor a 0"
                hectareas > SUPERFICIE_MAXIMA_HA -> "Máximo 100.000 ha"
                else -> null
            },
            zona = if (zona.isBlank()) "Elige la zona o usa el GPS simulado" else null,
            desde = if (desde == null) "Indica desde cuándo" else null,
            hasta = when {
                hasta == null -> "Indica hasta cuándo"
                desde != null && hasta < desde -> "No puede ser antes de la fecha inicial"
                else -> null
            },
            notas = if (notas.length > LARGO_MAXIMO_NOTAS) "Máximo 200 caracteres" else null
        )
    }

    fun perfilPrestador(
        nombre: String,
        servicios: Set<String>,
        cobertura: Set<String>,
        cultivos: Set<String>,
        capacidad: String,
        experiencia: String,
        desde: Long?,
        hasta: Long?
    ): ErroresPerfil {
        val capacidadNumero = capacidad.trim().toIntOrNull()
        val experienciaNumero = experiencia.trim().toIntOrNull()
        return ErroresPerfil(
            nombre = when {
                nombre.isBlank() -> "Escribe el nombre de tu empresa"
                nombre.trim().length < 3 -> "Usa al menos 3 caracteres"
                else -> null
            },
            servicios = if (servicios.isEmpty()) "Elige al menos un servicio" else null,
            cobertura = if (cobertura.isEmpty()) "Elige al menos una zona" else null,
            cultivos = if (cultivos.isEmpty()) "Elige al menos un cultivo" else null,
            capacidad = when {
                capacidad.isBlank() -> "Indica tu capacidad"
                capacidadNumero == null || capacidadNumero <= 0 -> "Debe ser mayor a 0"
                capacidadNumero > CAPACIDAD_MAXIMA_HA_DIA -> "Máximo 500 ha/día"
                else -> null
            },
            experiencia = when {
                experiencia.isBlank() -> "Indica tu experiencia"
                experienciaNumero == null -> "Ingresa un número"
                experienciaNumero > EXPERIENCIA_MAXIMA_ANIOS -> "Máximo 60 años"
                else -> null
            },
            desde = if (desde == null) "Indica desde cuándo" else null,
            hasta = when {
                hasta == null -> "Indica hasta cuándo"
                desde != null && hasta < desde -> "No puede ser antes de la fecha inicial"
                else -> null
            }
        )
    }

    fun nuevoPrestador(nombre: String, zona: String, servicio: String, capacidad: String): ErroresNuevoPrestador {
        val capacidadNumero = capacidad.trim().toIntOrNull()
        return ErroresNuevoPrestador(
            nombre = when {
                nombre.isBlank() -> "Escribe un nombre"
                nombre.trim().length < 3 -> "Usa al menos 3 caracteres"
                else -> null
            },
            zona = if (zona.isBlank()) "Elige una zona" else null,
            servicio = if (servicio.isBlank()) "Elige un servicio" else null,
            capacidad = when {
                capacidad.isBlank() -> "Indica la capacidad"
                capacidadNumero == null || capacidadNumero <= 0 -> "Debe ser mayor a 0"
                capacidadNumero > CAPACIDAD_MAXIMA_HA_DIA -> "Máximo 500 ha/día"
                else -> null
            }
        )
    }

    private fun errorDeCorreo(correo: String, mensajeVacio: String): String? = when {
        correo.isBlank() -> mensajeVacio
        !correoValido(correo) -> "El correo no es válido"
        else -> null
    }
}
