package com.example.cropfly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropfly.model.Catalogo
import com.example.cropfly.model.Fechas
import com.example.cropfly.model.Necesidad
import com.example.cropfly.model.Prestador
import com.example.cropfly.model.Roles
import com.example.cropfly.model.Usuario
import com.example.cropfly.repository.DatosDePruebaRepository
import com.example.cropfly.repository.NecesidadRepository
import com.example.cropfly.repository.PrestadorRepository
import com.example.cropfly.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PestanaAdmin(val etiqueta: String) {
    USUARIOS("Usuarios"),
    PRESTADORES("Prestadores"),
    NECESIDADES("Necesidades")
}

/** Qué elemento está esperando confirmación para borrarse. */
sealed interface EliminacionPendiente {
    data class DePrestador(val prestador: Prestador) : EliminacionPendiente
    data class DeNecesidad(val necesidad: Necesidad) : EliminacionPendiente
}

/** Formulario "Agregar prestador ficticio". */
data class NuevoPrestadorForm(
    val nombre: String = "",
    val zona: String = "",
    val servicio: String = "",
    val capacidad: String = "",
    val errores: ErroresNuevoPrestador = ErroresNuevoPrestador(),
    val intentoEnvio: Boolean = false
)

data class AdminUiState(
    val usuarios: List<Usuario> = emptyList(),
    val prestadores: List<Prestador> = emptyList(),
    val necesidades: List<Necesidad> = emptyList(),
    val pestana: PestanaAdmin = PestanaAdmin.USUARIOS,
    val formulario: NuevoPrestadorForm = NuevoPrestadorForm(),
    val eliminacionPendiente: EliminacionPendiente? = null,
    val mensaje: String? = null
) {
    val prestadoresActivos: Int get() = prestadores.count { it.activo }
}

/** Panel del administrador: gestiona los datos ficticios para probar el MVP. */
class AdminViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val prestadorRepository: PrestadorRepository,
    private val necesidadRepository: NecesidadRepository,
    private val datosDePruebaRepository: DatosDePruebaRepository
) : ViewModel() {

    private data class Local(
        val pestana: PestanaAdmin = PestanaAdmin.USUARIOS,
        val formulario: NuevoPrestadorForm = NuevoPrestadorForm(),
        val eliminacionPendiente: EliminacionPendiente? = null,
        val mensaje: String? = null
    )

    private val local = MutableStateFlow(Local())

    val estado: StateFlow<AdminUiState> = combine(
        usuarioRepository.observarTodos(),
        prestadorRepository.observarTodos(),
        necesidadRepository.observarTodas(),
        local
    ) { usuarios, prestadores, necesidades, estadoLocal ->
        AdminUiState(
            usuarios = usuarios,
            prestadores = prestadores,
            necesidades = necesidades,
            pestana = estadoLocal.pestana,
            formulario = estadoLocal.formulario,
            eliminacionPendiente = estadoLocal.eliminacionPendiente,
            mensaje = estadoLocal.mensaje
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminUiState())

    fun cambiarPestana(pestana: PestanaAdmin) = local.update { it.copy(pestana = pestana) }

    /** El administrador no puede desactivarse a sí mismo (ni a otro admin). */
    fun cambiarUsuarioActivo(usuario: Usuario, activo: Boolean) {
        if (usuario.rol == Roles.ADMIN) return
        viewModelScope.launch { usuarioRepository.cambiarActivo(usuario.id, activo) }
    }

    fun cambiarPrestadorActivo(prestador: Prestador, activo: Boolean) {
        viewModelScope.launch { prestadorRepository.cambiarActivo(prestador.id, activo) }
    }

    fun pedirEliminarPrestador(prestador: Prestador) =
        local.update { it.copy(eliminacionPendiente = EliminacionPendiente.DePrestador(prestador)) }

    fun pedirEliminarNecesidad(necesidad: Necesidad) =
        local.update { it.copy(eliminacionPendiente = EliminacionPendiente.DeNecesidad(necesidad)) }

    fun cancelarEliminacion() = local.update { it.copy(eliminacionPendiente = null) }

    fun confirmarEliminacion() {
        val pendiente = local.value.eliminacionPendiente ?: return
        local.update { it.copy(eliminacionPendiente = null) }
        viewModelScope.launch {
            when (pendiente) {
                is EliminacionPendiente.DePrestador -> prestadorRepository.eliminar(pendiente.prestador.id)
                is EliminacionPendiente.DeNecesidad -> necesidadRepository.eliminar(pendiente.necesidad.id)
            }
            mostrarMensaje("Elemento eliminado")
        }
    }

    fun onNombreNuevoChange(valor: String) = editarFormulario { it.copy(nombre = valor) }
    fun onZonaNuevoChange(valor: String) = editarFormulario { it.copy(zona = valor) }
    fun onServicioNuevoChange(valor: String) = editarFormulario { it.copy(servicio = valor) }
    fun onCapacidadNuevoChange(valor: String) =
        editarFormulario { it.copy(capacidad = valor.filter { c -> c.isDigit() }.take(3)) }

    fun agregarPrestador() {
        val formulario = local.value.formulario
        val errores = validar(formulario)
        local.update { it.copy(formulario = formulario.copy(intentoEnvio = true, errores = errores)) }
        if (errores.hayErrores) return

        viewModelScope.launch {
            val hoy = Fechas.hoy()
            prestadorRepository.agregar(
                Prestador(
                    nombre = formulario.nombre.trim(),
                    servicios = listOf(formulario.servicio),
                    cobertura = listOf(formulario.zona),
                    cultivos = Catalogo.cultivos,
                    capacidadHaDia = formulario.capacidad.trim().toInt(),
                    experienciaAnios = 1,
                    calificacion = 4.0,
                    disponibleDesde = hoy,
                    disponibleHasta = hoy + 90 * Fechas.MILIS_POR_DIA
                )
            )
            local.update { it.copy(formulario = NuevoPrestadorForm()) }
            mostrarMensaje("Prestador agregado")
        }
    }

    fun reiniciarDatosDePrueba() {
        viewModelScope.launch {
            datosDePruebaRepository.reiniciar()
            local.update { it.copy(formulario = NuevoPrestadorForm()) }
            mostrarMensaje("Datos de prueba restablecidos")
        }
    }

    fun consumirMensaje() = local.update { it.copy(mensaje = null) }

    private fun mostrarMensaje(texto: String) = local.update { it.copy(mensaje = texto) }

    private fun editarFormulario(cambio: (NuevoPrestadorForm) -> NuevoPrestadorForm) {
        local.update { actual ->
            val nuevo = cambio(actual.formulario)
            actual.copy(formulario = if (nuevo.intentoEnvio) nuevo.copy(errores = validar(nuevo)) else nuevo)
        }
    }

    private fun validar(formulario: NuevoPrestadorForm): ErroresNuevoPrestador =
        Validaciones.nuevoPrestador(formulario.nombre, formulario.zona, formulario.servicio, formulario.capacidad)
}
