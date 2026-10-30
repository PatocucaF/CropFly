package com.example.cropfly.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cropfly.ui.auth.AuthScreen
import com.example.cropfly.ui.auth.SplashScreen
import com.example.cropfly.ui.components.BotonSecundario
import com.example.cropfly.ui.components.rememberFabrica
import com.example.cropfly.viewmodel.AuthViewModel

/** Mapa de la app: qué pantalla se muestra en cada ruta y cómo se pasa de una a otra. */
@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val fabrica = rememberFabrica()
    val authViewModel: AuthViewModel = viewModel(factory = fabrica.auth())
    val sesion by authViewModel.sesion.collectAsState()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                estadoSesion = sesion,
                alTerminar = { usuario ->
                    val destino = if (usuario != null) Screen.inicioParaRol(usuario.rol) else Screen.Auth.route
                    navController.navigate(destino) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                estadoSesion = sesion,
                alIngresar = { usuario ->
                    navController.navigate(Screen.inicioParaRol(usuario.rol)) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantallas que se construyen en los pasos siguientes. Por ahora solo permiten probar la sesión guardada.
        val cerrarSesion = {
            authViewModel.cerrarSesion()
            navController.navigate(Screen.Auth.route) { popUpTo(0) }
        }
        composable(Screen.ProductorInicio.route) { PantallaTemporal("Inicio del productor", cerrarSesion) }
        composable(Screen.Solicitudes.route) { PantallaTemporal("Solicitudes del prestador", cerrarSesion) }
        composable(Screen.PanelAdmin.route) { PantallaTemporal("Panel del administrador", cerrarSesion) }
    }
}

/** Pantalla provisoria: confirma que el ingreso funciona y deja cerrar sesión para probar. Se reemplaza en los pasos 4 a 6. */
@Composable
private fun PantallaTemporal(titulo: String, alCerrarSesion: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo)
        BotonSecundario("Cerrar sesión", alCerrarSesion)
    }
}
