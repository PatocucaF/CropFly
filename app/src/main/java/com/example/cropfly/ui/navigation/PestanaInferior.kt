package com.example.cropfly.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cropfly.model.Roles

/** Cada botón de la barra inferior. La barra cambia según el perfil. */
enum class PestanaInferior(val etiqueta: String, val icono: ImageVector, val ruta: String) {
    INICIO("Inicio", Icons.Filled.Home, Screen.ProductorInicio.route),
    NECESIDAD("Necesidad", Icons.Filled.Add, Screen.NuevaNecesidad.route),
    RESULTADOS("Resultados", Icons.AutoMirrored.Filled.FormatListBulleted, Screen.Resultados.RUTA_SIN_ID),
    SOLICITUDES("Solicitudes", Icons.AutoMirrored.Filled.FormatListBulleted, Screen.Solicitudes.route),
    MI_PERFIL("Mi perfil", Icons.Filled.Eco, Screen.MiPerfil.route),
    PANEL("Panel", Icons.Filled.Security, Screen.PanelAdmin.route),
    CUENTA("Cuenta", Icons.Filled.Person, Screen.Cuenta.route);

    companion object {
        fun paraRol(rol: String): List<PestanaInferior> = when (rol) {
            Roles.PRODUCTOR -> listOf(INICIO, NECESIDAD, RESULTADOS, CUENTA)
            Roles.PRESTADOR -> listOf(SOLICITUDES, MI_PERFIL, CUENTA)
            else -> listOf(PANEL, CUENTA)
        }
    }
}
