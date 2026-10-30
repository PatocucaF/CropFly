package com.example.cropfly.repository

import com.example.cropfly.model.Prestador
import com.example.cropfly.model.Roles
import com.example.cropfly.model.Usuario
import com.example.cropfly.model.dao.PrestadorDao
import com.example.cropfly.model.dao.UsuarioDao
import com.example.cropfly.util.Seguridad
import kotlinx.coroutines.flow.Flow

/** Posibles resultados de intentar ingresar. */
sealed interface ResultadoIngreso {
    data class Exito(val usuario: Usuario) : ResultadoIngreso
    data object CredencialesInvalidas : ResultadoIngreso
    data object CuentaDesactivada : ResultadoIngreso
}

/** Cuentas y sesión. Es la única clase que habla con la tabla de usuarios. */
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val prestadorDao: PrestadorDao
) {
    /** Sesión guardada: devuelve al usuario que dejó la sesión abierta, o null si no hay. */
    suspend fun restaurarSesion(): Usuario? = usuarioDao.buscarSesionActiva()

    suspend fun iniciarSesion(correo: String, clave: String): ResultadoIngreso {
        val usuario = usuarioDao.buscarPorCorreo(normalizar(correo))
        return when {
            usuario == null || usuario.claveHash != Seguridad.hash(clave) -> ResultadoIngreso.CredencialesInvalidas
            !usuario.activo -> ResultadoIngreso.CuentaDesactivada
            else -> {
                // Solo una sesión abierta a la vez; queda guardada en Room.
                usuarioDao.cerrarTodasLasSesiones()
                usuarioDao.activarSesion(usuario.id)
                ResultadoIngreso.Exito(usuario.copy(sesionActiva = true))
            }
        }
    }

    suspend fun existeCorreo(correo: String): Boolean = usuarioDao.buscarPorCorreo(normalizar(correo)) != null

    /** Crea la cuenta y deja la sesión abierta. Si es prestador, crea también su perfil vacío. */
    suspend fun registrar(nombre: String, correo: String, clave: String, rol: String): Usuario {
        val nuevo = Usuario(
            nombre = nombre.trim(),
            correo = normalizar(correo),
            claveHash = Seguridad.hash(clave),
            rol = rol
        )
        val id = usuarioDao.insertar(nuevo)
        if (rol == Roles.PRESTADOR) {
            prestadorDao.insertar(Prestador(usuarioId = id, nombre = nuevo.nombre))
        }
        usuarioDao.cerrarTodasLasSesiones()
        usuarioDao.activarSesion(id)
        return nuevo.copy(id = id, sesionActiva = true)
    }

    suspend fun cerrarSesion() = usuarioDao.cerrarTodasLasSesiones()

    suspend fun cambiarActivo(id: Long, activo: Boolean) = usuarioDao.cambiarActivo(id, activo)

    suspend fun cambiarUbicacionPermitida(id: Long, permitida: Boolean) =
        usuarioDao.cambiarUbicacionPermitida(id, permitida)

    fun observarTodos(): Flow<List<Usuario>> = usuarioDao.observarTodos()

    fun observarPorId(id: Long): Flow<Usuario?> = usuarioDao.observarPorId(id)

    private fun normalizar(correo: String): String = correo.trim().lowercase()
}
