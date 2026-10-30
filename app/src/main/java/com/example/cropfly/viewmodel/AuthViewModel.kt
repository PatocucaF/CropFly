package com.example.cropfly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropfly.model.Roles
import com.example.cropfly.model.Usuario
import com.example.cropfly.repository.DatosDePruebaRepository
import com.example.cropfly.repository.ResultadoIngreso
import com.example.cropfly.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Qué pasa con la sesión: todavía no se sabe, no hay sesión guardada, o hay un usuario adentro. */
sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Activa(val usuario: Usuario) : EstadoSesion
}

enum class ModoAuth { INGRESAR, CREAR_CUENTA }

data class AuthUiState(
    val modo: ModoAuth = ModoAuth.INGRESAR,
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val repetirClave: String = "",
    val rol: String = Roles.PRODUCTOR,
    val aceptaTerminos: Boolean = false,
    val errores: ErroresAuth = ErroresAuth(),
    val intentoEnvio: Boolean = false,
    val cargando: Boolean = false
)

/**
 * Login, registro y sesión guardada.
 * La pantalla solo muestra [estado] y avisa de lo que hace el usuario; todas las reglas están aquí.
 */
class AuthViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _estado = MutableStateFlow(AuthUiState())
    val estado: StateFlow<AuthUiState> = _estado.asStateFlow()

    private val _sesion = MutableStateFlow<EstadoSesion>(EstadoSesion.Cargando)
    val sesion: StateFlow<EstadoSesion> = _sesion.asStateFlow()

    init {
        restaurarSesion()
    }

    /** Si el usuario dejó la sesión abierta, Room lo recuerda y se entra directo. */
    private fun restaurarSesion() {
        viewModelScope.launch {
            val usuario = usuarioRepository.restaurarSesion()
            _sesion.value = if (usuario != null) EstadoSesion.Activa(usuario) else EstadoSesion.SinSesion
        }
    }

    fun cambiarModo(modo: ModoAuth) {
        _estado.value = AuthUiState(modo = modo)
    }

    fun onNombreChange(valor: String) = editar { it.copy(nombre = valor) }
    fun onCorreoChange(valor: String) = editar { it.copy(correo = valor) }
    fun onClaveChange(valor: String) = editar { it.copy(clave = valor) }
    fun onRepetirClaveChange(valor: String) = editar { it.copy(repetirClave = valor) }
    fun onRolChange(rol: String) = editar { it.copy(rol = rol) }
    fun onAceptaTerminosChange(acepta: Boolean) = editar { it.copy(aceptaTerminos = acepta) }

    /** Los botones "Cuentas demo" rellenan el formulario para entrar rápido. */
    fun usarCuentaDemo(rol: String) {
        val correo = when (rol) {
            Roles.PRODUCTOR -> DatosDePruebaRepository.CORREO_PRODUCTOR_DEMO
            Roles.PRESTADOR -> DatosDePruebaRepository.CORREO_PRESTADOR_DEMO
            else -> DatosDePruebaRepository.CORREO_ADMIN_DEMO
        }
        _estado.value = AuthUiState(
            modo = ModoAuth.INGRESAR,
            correo = correo,
            clave = DatosDePruebaRepository.CLAVE_DEMO
        )
    }

    fun ingresar() {
        val actual = _estado.value
        if (actual.cargando) return
        val errores = Validaciones.ingreso(actual.correo, actual.clave)
        _estado.update { it.copy(intentoEnvio = true, errores = errores) }
        if (errores.hayErrores) return

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            when (val resultado = usuarioRepository.iniciarSesion(actual.correo, actual.clave)) {
                is ResultadoIngreso.Exito -> terminarConSesion(resultado.usuario)
                is ResultadoIngreso.CredencialesInvalidas -> mostrarErrorGeneral("Correo o contraseña incorrectos")
                is ResultadoIngreso.CuentaDesactivada ->
                    mostrarErrorGeneral("Tu cuenta está desactivada. Consulta al administrador.")
            }
        }
    }

    fun registrar() {
        val actual = _estado.value
        if (actual.cargando) return
        val errores = Validaciones.registro(
            actual.nombre, actual.correo, actual.clave, actual.repetirClave, actual.aceptaTerminos
        )
        _estado.update { it.copy(intentoEnvio = true, errores = errores) }
        if (errores.hayErrores) return

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            if (usuarioRepository.existeCorreo(actual.correo)) {
                _estado.update {
                    it.copy(cargando = false, errores = ErroresAuth(correo = "Ese correo ya está registrado"))
                }
            } else {
                val usuario = usuarioRepository.registrar(actual.nombre, actual.correo, actual.clave, actual.rol)
                terminarConSesion(usuario)
            }
        }
    }

    fun cerrarSesion() {
        // Primero se actualiza el estado en memoria para que la pantalla de login no vuelva a entrar sola.
        _sesion.value = EstadoSesion.SinSesion
        _estado.value = AuthUiState()
        viewModelScope.launch { usuarioRepository.cerrarSesion() }
    }

    private fun terminarConSesion(usuario: Usuario) {
        _estado.value = AuthUiState()
        _sesion.value = EstadoSesion.Activa(usuario)
    }

    private fun mostrarErrorGeneral(mensaje: String) {
        _estado.update { it.copy(cargando = false, errores = ErroresAuth(general = mensaje)) }
    }

    /** Aplica el cambio y, si ya se intentó enviar, vuelve a validar para que los errores se actualicen al escribir. */
    private fun editar(cambio: (AuthUiState) -> AuthUiState) {
        _estado.update { actual ->
            val nuevo = cambio(actual)
            if (nuevo.intentoEnvio) nuevo.copy(errores = validar(nuevo)) else nuevo
        }
    }

    private fun validar(estado: AuthUiState): ErroresAuth =
        if (estado.modo == ModoAuth.INGRESAR) {
            Validaciones.ingreso(estado.correo, estado.clave)
        } else {
            Validaciones.registro(
                estado.nombre, estado.correo, estado.clave, estado.repetirClave, estado.aceptaTerminos
            )
        }
}
