package com.example.cropfly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropfly.model.Notificacion
import com.example.cropfly.repository.NotificacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificacionesUiState(
    val notificaciones: List<Notificacion> = emptyList(),
    val sinLeer: Int = 0,
    val panelAbierto: Boolean = false
)

/** La campanita de la barra superior: contador de avisos nuevos y panel con la lista. */
class NotificacionesViewModel(
    private val usuarioId: Long,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    private val panelAbierto = MutableStateFlow(false)

    val estado: StateFlow<NotificacionesUiState> = combine(
        notificacionRepository.observarPorUsuario(usuarioId),
        panelAbierto
    ) { notificaciones, abierto ->
        NotificacionesUiState(
            notificaciones = notificaciones,
            sinLeer = notificaciones.count { !it.leida },
            panelAbierto = abierto
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificacionesUiState())

    fun abrirPanel() {
        panelAbierto.value = true
    }

    /** Al cerrar el panel (botón "Listo") todos los avisos quedan como leídos. */
    fun cerrarPanel() {
        panelAbierto.value = false
        viewModelScope.launch { notificacionRepository.marcarTodasLeidas(usuarioId) }
    }
}
