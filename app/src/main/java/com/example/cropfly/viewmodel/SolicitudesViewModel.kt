package com.example.cropfly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropfly.model.Prestador
import com.example.cropfly.model.SolicitudRecibida
import com.example.cropfly.repository.InteresRepository
import com.example.cropfly.repository.PrestadorRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SolicitudesUiState(
    val prestador: Prestador? = null,
    val solicitudes: List<SolicitudRecibida> = emptyList()
) {
    /** El prestador es visible para los productores solo si está activo y ya completó su perfil. */
    val visibleParaProductores: Boolean
        get() = prestador != null && prestador.activo && prestador.perfilCompleto
}

/** Pantalla "Solicitudes" del prestador: los intereses que le enviaron los productores. */
class SolicitudesViewModel(
    usuarioId: Long,
    prestadorRepository: PrestadorRepository,
    private val interesRepository: InteresRepository
) : ViewModel() {

    private val prestadorFlow: Flow<Prestador?> = prestadorRepository.observarPorUsuario(usuarioId)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val solicitudesFlow: Flow<List<SolicitudRecibida>> = prestadorFlow.flatMapLatest { prestador ->
        if (prestador == null) emptyFlow<List<SolicitudRecibida>>() else interesRepository.observarRecibidos(prestador.id)
    }

    val estado: StateFlow<SolicitudesUiState> = combine(prestadorFlow, solicitudesFlow) { prestador, solicitudes ->
        SolicitudesUiState(prestador, solicitudes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SolicitudesUiState())

    fun marcarComoVista(interesId: Long) {
        viewModelScope.launch { interesRepository.marcarVista(interesId) }
    }
}
