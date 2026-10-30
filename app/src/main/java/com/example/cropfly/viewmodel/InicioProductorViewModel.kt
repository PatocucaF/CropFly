package com.example.cropfly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropfly.model.Matching
import com.example.cropfly.model.Necesidad
import com.example.cropfly.repository.InteresRepository
import com.example.cropfly.repository.NecesidadRepository
import com.example.cropfly.repository.PrestadorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Una necesidad junto con cuántos prestadores compatibles tiene hoy. */
data class NecesidadConResultados(val necesidad: Necesidad, val compatibles: Int)

data class InicioProductorUiState(
    val necesidades: List<NecesidadConResultados> = emptyList(),
    val conResultados: Int = 0,
    val intereses: Int = 0
)

/** Pantalla "Inicio" del productor: resumen, lista de necesidades y borrado con confirmación. */
class InicioProductorViewModel(
    private val productorId: Long,
    private val necesidadRepository: NecesidadRepository,
    prestadorRepository: PrestadorRepository,
    interesRepository: InteresRepository
) : ViewModel() {

    private val _necesidadPorEliminar = MutableStateFlow<Necesidad?>(null)

    /** Si no es null, la pantalla muestra el diálogo "¿Eliminar necesidad?". */
    val necesidadPorEliminar: StateFlow<Necesidad?> = _necesidadPorEliminar

    val estado: StateFlow<InicioProductorUiState> = combine(
        necesidadRepository.observarPorProductor(productorId),
        prestadorRepository.observarTodos(),
        interesRepository.observarPorProductor(productorId)
    ) { necesidades, prestadores, intereses ->
        val conCompatibles = necesidades.map { necesidad ->
            NecesidadConResultados(necesidad, Matching.buscar(necesidad, prestadores).size)
        }
        InicioProductorUiState(
            necesidades = conCompatibles,
            conResultados = conCompatibles.count { it.compatibles > 0 },
            intereses = intereses.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InicioProductorUiState())

    fun pedirEliminar(necesidad: Necesidad) {
        _necesidadPorEliminar.value = necesidad
    }

    fun cancelarEliminar() {
        _necesidadPorEliminar.value = null
    }

    fun confirmarEliminar() {
        val necesidad = _necesidadPorEliminar.value ?: return
        _necesidadPorEliminar.value = null
        viewModelScope.launch { necesidadRepository.eliminar(necesidad.id) }
    }
}
