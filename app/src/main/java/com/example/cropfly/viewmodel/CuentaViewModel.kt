package com.example.cropfly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropfly.model.Usuario
import com.example.cropfly.repository.UsuarioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Pantalla "Mi cuenta": datos del usuario y el permiso de ubicación simulada. */
class CuentaViewModel(
    private val usuarioId: Long,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = usuarioRepository.observarPorId(usuarioId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun cambiarUbicacionPermitida(permitida: Boolean) {
        viewModelScope.launch { usuarioRepository.cambiarUbicacionPermitida(usuarioId, permitida) }
    }
}
