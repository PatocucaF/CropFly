package com.example.cropfly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropfly.model.Catalogo
import com.example.cropfly.model.Necesidad
import com.example.cropfly.repository.NecesidadRepository
import com.example.cropfly.repository.UsuarioRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NuevaNecesidadUiState(
    val servicio: String = "",
    val cultivo: String = "",
    val superficie: String = "",
    val zona: String = "",
    val usaGps: Boolean = false,
    val desde: Long? = null,
    val hasta: Long? = null,
    val producto: String = "",
    val notas: String = "",
    val fotoPath: String? = null,
    val errorFoto: String? = null,
    val avisoUbicacion: String? = null,
    val mostrarPermisoUbicacion: Boolean = false,
    val intentoEnvio: Boolean = false,
    val buscando: Boolean = false,
    /** Cuando se guarda la necesidad, aquí queda su id y la pantalla navega a "Resultados". */
    val necesidadCreadaId: Long? = null
) {
    /**
     * Resultado de validar lo escrito hasta ahora (siempre calculado).
     * La pantalla muestra los mensajes solo cuando [intentoEnvio] es true, y usa los campos sin error para el check verde.
     */
    val errores: ErroresNecesidad
        get() = Validaciones.necesidad(servicio, cultivo, superficie, zona, desde, hasta, notas)
}

/** Formulario "Nueva necesidad": validación, GPS simulado, foto del predio y guardado. */
class NuevaNecesidadViewModel(
    private val productorId: Long,
    private val necesidadRepository: NecesidadRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(NuevaNecesidadUiState())
    val estado: StateFlow<NuevaNecesidadUiState> = _estado.asStateFlow()

    fun onServicioChange(valor: String) = editar { it.copy(servicio = valor) }
    fun onCultivoChange(valor: String) = editar { it.copy(cultivo = valor) }
    fun onSuperficieChange(valor: String) = editar { it.copy(superficie = valor.filter { c -> c.isDigit() }.take(6)) }
    fun onZonaChange(valor: String) = editar { it.copy(zona = valor, usaGps = false, avisoUbicacion = null) }
    fun onDesdeChange(valor: Long) = editar { it.copy(desde = valor) }
    fun onHastaChange(valor: Long) = editar { it.copy(hasta = valor) }
    fun onProductoChange(valor: String) = editar { it.copy(producto = valor) }
    fun onNotasChange(valor: String) = editar { it.copy(notas = valor) }

    fun onFotoElegida(ruta: String) = editar { it.copy(fotoPath = ruta, errorFoto = null) }
    fun onFotoError() = editar { it.copy(errorFoto = "No se pudo cargar la imagen. Prueba con otra.") }
    fun quitarFoto() = editar { it.copy(fotoPath = null, errorFoto = null) }

    /**
     * Botón "Usar GPS (simulado)". Si el usuario ya dio el permiso simulado, se aplica de inmediato;
     * si no, la pantalla muestra el modal "¿Permitir ubicación?".
     */
    fun usarGps() {
        viewModelScope.launch {
            val usuario = usuarioRepository.observarPorId(productorId).first()
            if (usuario?.ubicacionPermitida == true) {
                aplicarUbicacionSimulada()
            } else {
                _estado.update { it.copy(mostrarPermisoUbicacion = true) }
            }
        }
    }

    fun responderPermisoUbicacion(permitir: Boolean) {
        if (permitir) {
            viewModelScope.launch { usuarioRepository.cambiarUbicacionPermitida(productorId, true) }
            aplicarUbicacionSimulada()
        } else {
            _estado.update {
                it.copy(
                    mostrarPermisoUbicacion = false,
                    avisoUbicacion = "Sin permiso de ubicación: elige la zona de la lista."
                )
            }
        }
    }

    private fun aplicarUbicacionSimulada() = editar {
        it.copy(
            zona = Catalogo.ZONA_GPS_SIMULADO,
            usaGps = true,
            mostrarPermisoUbicacion = false,
            avisoUbicacion = null
        )
    }

    /** Botón "Buscar prestadores": valida, guarda la necesidad y avisa a la pantalla para navegar. */
    fun buscarPrestadores() {
        val actual = _estado.value
        if (actual.buscando) return
        _estado.update { it.copy(intentoEnvio = true) }
        if (actual.errores.hayErrores) return

        viewModelScope.launch {
            _estado.update { it.copy(buscando = true) }
            delay(1_200) // Pequeña pausa para que se vea la animación de "buscando".
            val zona = Catalogo.zonaPorNombre(actual.zona)
            val id = necesidadRepository.crear(
                Necesidad(
                    productorId = productorId,
                    servicio = actual.servicio,
                    cultivo = actual.cultivo,
                    superficieHa = actual.superficie.trim().toInt(),
                    zona = actual.zona,
                    latitud = zona?.latitud,
                    longitud = zona?.longitud,
                    desde = actual.desde ?: 0L,
                    hasta = actual.hasta ?: 0L,
                    producto = actual.producto,
                    notas = actual.notas.trim(),
                    fotoPath = actual.fotoPath
                )
            )
            _estado.update { it.copy(buscando = false, necesidadCreadaId = id) }
        }
    }

    /** La pantalla ya navegó: se limpia el formulario para la próxima necesidad. */
    fun consumirNavegacion() {
        _estado.value = NuevaNecesidadUiState()
    }

    private fun editar(cambio: (NuevaNecesidadUiState) -> NuevaNecesidadUiState) {
        _estado.update(cambio)
    }
}
