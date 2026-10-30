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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cropfly.ui.auth.AuthScreen
import com.example.cropfly.ui.auth.SplashScreen
import com.example.cropfly.ui.components.BotonSecundario
import com.example.cropfly.ui.components.rememberFabrica
import com.example.cropfly.ui.producer.InicioProductorScreen
import com.example.cropfly.ui.producer.NuevaNecesidadScreen
import com.example.cropfly.viewmodel.AuthViewModel
import com.example.cropfly.viewmodel.EstadoSesion
import com.example.cropfly.viewmodel.InicioProductorViewModel
import com.example.cropfly.viewmodel.NuevaNecesidadViewModel

/** Mapa de la app: qué pantalla se muestra en cada ruta y cómo se pasa de una a otra. */
@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val fabrica = rememberFabrica()
    val authViewModel: AuthViewModel = viewModel(factory = fabrica.auth())
    val sesion by authViewModel.sesion.collectAsState()
    val usuarioActual = (sesion as? EstadoSesion.Activa)?.usuario

    // Cambia de pestaña de la barra inferior sin apilar pantallas repetidas.
    fun irAPestana(pestana: PestanaInferior, rol: String) {
        navController.navigate(pestana.ruta) {
            popUpTo(Screen.inicioParaRol(rol))
            launchSingleTop = true
        }
    }

    val cerrarSesion = {
        authViewModel.cerrarSesion()
        navController.navigate(Screen.Auth.route) { popUpTo(0) }
    }

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

        // ---------- Productor ----------
        composable(Screen.ProductorInicio.route) {
            val usuario = usuarioActual ?: return@composable
            val viewModel: InicioProductorViewModel = viewModel(factory = fabrica.inicioProductor(usuario.id))
            InicioProductorScreen(
                usuarioId = usuario.id,
                viewModel = viewModel,
                alElegirPestana = { irAPestana(it, usuario.rol) },
                alRegistrarNecesidad = { navController.navigate(Screen.NuevaNecesidad.route) },
                alVerResultados = { necesidadId ->
                    navController.navigate(Screen.Resultados.conId(necesidadId))
                }
            )
        }
        composable(Screen.NuevaNecesidad.route) {
            val usuario = usuarioActual ?: return@composable
            val viewModel: NuevaNecesidadViewModel = viewModel(factory = fabrica.nuevaNecesidad(usuario.id))
            NuevaNecesidadScreen(
                usuarioId = usuario.id,
                viewModel = viewModel,
                alElegirPestana = { irAPestana(it, usuario.rol) },
                alVolver = { navController.popBackStack() },
                alCrearNecesidad = { necesidadId ->
                    navController.navigate(Screen.Resultados.conId(necesidadId)) {
                        popUpTo(Screen.ProductorInicio.route)
                    }
                }
            )
        }

        // Pantallas que se construyen en los pasos siguientes (5 y 6).
        composable(
            route = Screen.Resultados.route,
            arguments = listOf(
                navArgument(Screen.Resultados.ARGUMENTO_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { PantallaTemporal("Resultados", cerrarSesion) }
        composable(Screen.Cuenta.route) { PantallaTemporal("Mi cuenta", cerrarSesion) }
        composable(Screen.Solicitudes.route) { PantallaTemporal("Solicitudes del prestador", cerrarSesion) }
        composable(Screen.PanelAdmin.route) { PantallaTemporal("Panel del administrador", cerrarSesion) }
    }
}

/** Pantalla provisoria: deja cerrar sesión para probar. Se reemplaza en los pasos 5 y 6. */
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
