package com.example.cropfly.ui.navigation

import com.example.cropfly.model.Roles

/** Todas las pantallas de la app y su "dirección" (ruta) dentro del NavHost. */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Auth : Screen("auth")

    // Productor
    data object ProductorInicio : Screen("productor_inicio")
    data object NuevaNecesidad : Screen("nueva_necesidad")

    /** "necesidadId" es opcional: sin id muestra los resultados de la necesidad más reciente. */
    data object Resultados : Screen("resultados?necesidadId={necesidadId}") {
        const val RUTA_SIN_ID = "resultados"
        const val ARGUMENTO_ID = "necesidadId"
        fun conId(necesidadId: Long): String = "resultados?necesidadId=$necesidadId"
    }

    data object Comparar : Screen("comparar")

    // Prestador
    data object Solicitudes : Screen("solicitudes")
    data object MiPerfil : Screen("mi_perfil")

    // Administrador
    data object PanelAdmin : Screen("panel_admin")

    // Comparten los tres perfiles
    data object Cuenta : Screen("cuenta")

    companion object {
        /** Pantalla de inicio según el perfil con el que se ingresó. */
        fun inicioParaRol(rol: String): String = when (rol) {
            Roles.PRODUCTOR -> ProductorInicio.route
            Roles.PRESTADOR -> Solicitudes.route
            else -> PanelAdmin.route
        }
    }
}
