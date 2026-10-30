package com.example.cropfly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropfly.model.Matching
import com.example.cropfly.model.Necesidad
import com.example.cropfly.model.ResultadoMatch
import com.example.cropfly.repository.InteresRepository
import com.example.cropfly.repository.NecesidadRepository
import com.example.cropfly.repository.PrestadorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FiltroResultados(val etiqueta: String) {
    TODOS("Todos"),
    MI_ZONA("Mi zona"),
    DISPONIBLES("Disponibles en mi ventana")
}

/** Lo que el usuario ha ido haciendo en la pantalla (filtro, selección, modales abiertos). */
private data class InteraccionResultados(
    val filtro: FiltroResultados = FiltroResultados.TODOS,
    val seleccionados: Set<Long> = emptySet(),
    val detalleId: Long? = null,
    val interesPendienteId: Long? = null,
    val avisoInteres: String? = null
)

data class ResultadosUiState(
    val cargando: Boolean = true,
    val necesidad: Necesidad? = null,
    val resultados: List<ResultadoMatch> = emptyList(),
    val filtro: FiltroResultados = FiltroResultados.TODOS,
    val seleccionados: Set<Long> = emptySet(),
    val interesesEnviados: Set<Long> = emptySet(),
    val detalle: ResultadoMatch? = null,
    val interesPendiente: ResultadoMatch? = null,
    /** Nombre del prestador al que se acaba de enviar interés (la pantalla lanza la notificación nativa). */
    val avisoInteres: String? = null
) {
    /** Lista que se ve según el filtro elegido. */
    val visibles: List<ResultadoMatch>
        get() = when (filtro) {
            FiltroResultados.TODOS -> resultados
            FiltroResultados.MI_ZONA -> resultados.filter { it.zona }
            FiltroResultados.DISPONIBLES -> resultados.filter { it.disponible }
        }

    /** Prestadores marcados para comparar, en el mismo orden del ranking. */
    val comparados: List<ResultadoMatch>
        get() = resultados.filter { it.prestador.id in seleccionados }

    val puedeComparar: Boolean
        get() = seleccionados.size >= ResultadosViewModel.MINIMO_PARA_COMPARAR

    // Lo mejor de cada fila de la tabla de comparación (se marca con la etiqueta "MEJOR").
    val mejorAfinidad: Int? get() = comparados.maxOfOrNull { it.afinidad }
    val mejorCapacidad: Int? get() = comparados.maxOfOrNull { it.prestador.capacidadHaDia }
    val mejorExperiencia: Int? get() = comparados.maxOfOrNull { it.prestador.experienciaAnios }
    val mejorCalificacion: Double? get() = comparados.maxOfOrNull { it.prestador.calificacion }
}

/**
 * Resultados del matching, comparación y "Me interesa".
 * Si necesidadId es null se usa la necesidad más reciente del productor (pestaña "Resultados").
 * Las pantallas "Resultados" y "Comparar" comparten este mismo ViewModel.
 */
class ResultadosViewModel(
    private val productorId: Long,
    necesidadId: Long?,
    necesidadRepository: NecesidadRepository,
    prestadorRepository: PrestadorRepository,
    private val interesRepository: InteresRepository
) : ViewModel() {

    private val interaccion = MutableStateFlow(InteraccionResultados())

    private val necesidadFlow: Flow<Necesidad?> =
        if (necesidadId != null) {
            flow { emit(necesidadRepository.buscarPorId(necesidadId)) }
        } else {
            necesidadRepository.observarPorProductor(productorId).map { it.firstOrNull() }
        }

    val estado: StateFlow<ResultadosUiState> = combine(
        necesidadFlow,
        prestadorRepository.observarTodos(),
        interesRepository.observarPorProductor(productorId),
        interaccion
    ) { necesidad, prestadores, intereses, accion ->
        val resultados = if (necesidad != null) Matching.buscar(necesidad, prestadores) else emptyList()
        val enviados = intereses
            .filter { it.necesidadId == necesidad?.id }
            .map { it.prestadorId }
            .toSet()
        ResultadosUiState(
            cargando = false,
            necesidad = necesidad,
            resultados = resultados,
            filtro = accion.filtro,
            seleccionados = accion.seleccionados,
            interesesEnviados = enviados,
            detalle = resultados.firstOrNull { it.prestador.id == accion.detalleId },
            interesPendiente = resultados.firstOrNull { it.prestador.id == accion.interesPendienteId },
            avisoInteres = accion.avisoInteres
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultadosUiState())

    fun cambiarFiltro(filtro: FiltroResultados) = interaccion.update { it.copy(filtro = filtro) }

    /** Marca o desmarca un prestador para comparar (máximo 3 a la vez). */
    fun alternarSeleccion(prestadorId: Long) = interaccion.update { actual ->
        val nuevos = when {
            prestadorId in actual.seleccionados -> actual.seleccionados - prestadorId
            actual.seleccionados.size < MAXIMO_PARA_COMPARAR -> actual.seleccionados + prestadorId
            else -> actual.seleccionados
        }
        actual.copy(seleccionados = nuevos)
    }

    fun abrirDetalle(prestadorId: Long) = interaccion.update { it.copy(detalleId = prestadorId) }
    fun cerrarDetalle() = interaccion.update { it.copy(detalleId = null) }

    /** "Me interesa" abre el modal de confirmación. */
    fun pedirInteres(prestadorId: Long) =
        interaccion.update { it.copy(interesPendienteId = prestadorId, detalleId = null) }

    fun cancelarInteres() = interaccion.update { it.copy(interesPendienteId = null) }

    fun confirmarInteres() {
        val actual = estado.value
        val necesidad = actual.necesidad ?: return
        val prestador = actual.interesPendiente?.prestador ?: return
        interaccion.update { it.copy(interesPendienteId = null) }
        viewModelScope.launch {
            val enviado = interesRepository.enviar(necesidad, prestador, productorId)
            if (enviado) interaccion.update { it.copy(avisoInteres = prestador.nombre) }
        }
    }

    /** La pantalla ya mostró la notificación nativa. */
    fun consumirAvisoInteres() = interaccion.update { it.copy(avisoInteres = null) }

    companion object {
        const val MINIMO_PARA_COMPARAR = 2
        const val MAXIMO_PARA_COMPARAR = 3
    }
}
