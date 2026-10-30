package com.example.cropfly.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cropfly.ContenedorDeDependencias

/**
 * Los ViewModels necesitan repositorios (y a veces el id del usuario), y Android no sabe pasárselos solo.
 * Esta clase le enseña a Android cómo construir cada uno, usando el contenedor de dependencias.
 */
class FabricaDeViewModels(private val contenedor: ContenedorDeDependencias) {

    fun auth(): ViewModelProvider.Factory = viewModelFactory {
        initializer { AuthViewModel(contenedor.usuarioRepository) }
    }

    fun inicioProductor(productorId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            InicioProductorViewModel(
                productorId,
                contenedor.necesidadRepository,
                contenedor.prestadorRepository,
                contenedor.interesRepository
            )
        }
    }

    fun nuevaNecesidad(productorId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            NuevaNecesidadViewModel(productorId, contenedor.necesidadRepository, contenedor.usuarioRepository)
        }
    }

    fun resultados(productorId: Long, necesidadId: Long?): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ResultadosViewModel(
                productorId,
                necesidadId,
                contenedor.necesidadRepository,
                contenedor.prestadorRepository,
                contenedor.interesRepository
            )
        }
    }

    fun notificaciones(usuarioId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer { NotificacionesViewModel(usuarioId, contenedor.notificacionRepository) }
    }

    fun solicitudes(usuarioId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            SolicitudesViewModel(usuarioId, contenedor.prestadorRepository, contenedor.interesRepository)
        }
    }

    fun perfilPrestador(usuarioId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer { PerfilPrestadorViewModel(usuarioId, contenedor.prestadorRepository) }
    }

    fun admin(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AdminViewModel(
                contenedor.usuarioRepository,
                contenedor.prestadorRepository,
                contenedor.necesidadRepository,
                contenedor.datosDePruebaRepository
            )
        }
    }

    fun cuenta(usuarioId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer { CuentaViewModel(usuarioId, contenedor.usuarioRepository) }
    }
}
