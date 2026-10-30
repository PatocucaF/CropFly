package com.example.cropfly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropfly.model.Prestador
import com.example.cropfly.repository.PrestadorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val cargado: Boolean = false,
    val nombre: String = "",
    val servicios: Set<String> = emptySet(),
    val cobertura: Set<String> = emptySet(),
    val cultivos: Set<String> = emptySet(),
    val capacidad: String = "",
    val experiencia: String = "",
    val desde: Long? = null,
    val hasta: Long? = null,
    val errores: ErroresPerfil = ErroresPerfil(),
    val intentoEnvio: Boolean = false,
    /** true cuando se acaba de guardar: la pantalla muestra el aviso y lo consume. */
    val guardado: Boolean = false
)

/** Pantalla "Mi perfil" del prestador: edita el perfil demostrativo que ven los productores. */
class PerfilPrestadorViewModel(
    private val usuarioId: Long,
    private val prestadorRepository: PrestadorRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(PerfilUiState())
    val estado: StateFlow<PerfilUiState> = _estado.asStateFlow()

    private var perfilOriginal: Prestador? = null

    init {
        viewModelScope.launch {
            val perfil = prestadorRepository.observarPorUsuario(usuarioId).first()
            perfilOriginal = perfil
            if (perfil != null) {
                _estado.value = PerfilUiState(
                    cargado = true,
                    nombre = perfil.nombre,
                    servicios = perfil.servicios.toSet(),
                    cobertura = perfil.cobertura.toSet(),
                    cultivos = perfil.cultivos.toSet(),
                    capacidad = if (perfil.capacidadHaDia > 0) perfil.capacidadHaDia.toString() else "",
                    experiencia = if (perfil.capacidadHaDia > 0) perfil.experienciaAnios.toString() else "",
                    desde = perfil.disponibleDesde.takeIf { it > 0L },
                    hasta = perfil.disponibleHasta.takeIf { it > 0L }
                )
            } else {
                _estado.update { it.copy(cargado = true) }
            }
        }
    }

    fun onNombreChange(valor: String) = editar { it.copy(nombre = valor) }
    fun onCapacidadChange(valor: String) = editar { it.copy(capacidad = valor.filter { c -> c.isDigit() }.take(3)) }
    fun onExperienciaChange(valor: String) = editar { it.copy(experiencia = valor.filter { c -> c.isDigit() }.take(2)) }
    fun onDesdeChange(valor: Long) = editar { it.copy(desde = valor) }
    fun onHastaChange(valor: Long) = editar { it.copy(hasta = valor) }

    fun alternarServicio(servicio: String) = editar { it.copy(servicios = alternar(it.servicios, servicio)) }
    fun alternarZona(zona: String) = editar { it.copy(cobertura = alternar(it.cobertura, zona)) }
    fun alternarCultivo(cultivo: String) = editar { it.copy(cultivos = alternar(it.cultivos, cultivo)) }

    fun guardar() {
        val actual = _estado.value
        val original = perfilOriginal ?: return
        val errores = validar(actual)
        _estado.update { it.copy(intentoEnvio = true, errores = errores) }
        if (errores.hayErrores) return

        viewModelScope.launch {
            val actualizado = original.copy(
                nombre = actual.nombre.trim(),
                servicios = actual.servicios.toList(),
                cobertura = actual.cobertura.toList(),
                cultivos = actual.cultivos.toList(),
                capacidadHaDia = actual.capacidad.trim().toInt(),
                experienciaAnios = actual.experiencia.trim().toInt(),
                disponibleDesde = actual.desde ?: 0L,
                disponibleHasta = actual.hasta ?: 0L,
                // Un perfil nuevo parte con una calificación demostrativa neutra.
                calificacion = if (original.calificacion > 0.0) original.calificacion else 4.0
            )
            prestadorRepository.actualizar(actualizado)
            perfilOriginal = actualizado
            _estado.update { it.copy(guardado = true) }
        }
    }

    fun consumirGuardado() = _estado.update { it.copy(guardado = false) }

    private fun alternar(conjunto: Set<String>, elemento: String): Set<String> =
        if (elemento in conjunto) conjunto - elemento else conjunto + elemento

    private fun editar(cambio: (PerfilUiState) -> PerfilUiState) {
        _estado.update { actual ->
            val nuevo = cambio(actual)
            if (nuevo.intentoEnvio) nuevo.copy(errores = validar(nuevo)) else nuevo
        }
    }

    private fun validar(estado: PerfilUiState): ErroresPerfil = Validaciones.perfilPrestador(
        nombre = estado.nombre,
        servicios = estado.servicios,
        cobertura = estado.cobertura,
        cultivos = estado.cultivos,
        capacidad = estado.capacidad,
        experiencia = estado.experiencia,
        desde = estado.desde,
        hasta = estado.hasta
    )
}
