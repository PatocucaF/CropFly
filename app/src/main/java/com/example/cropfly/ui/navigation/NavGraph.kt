package com.example.cropfly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cropfly.ui.account.CuentaScreen
import com.example.cropfly.ui.admin.PanelAdminScreen
import com.example.cropfly.ui.auth.AuthScreen
import com.example.cropfly.ui.auth.SplashScreen
import com.example.cropfly.ui.components.rememberFabrica
import com.example.cropfly.ui.producer.CompararScreen
import com.example.cropfly.ui.producer.InicioProductorScreen
import com.example.cropfly.ui.producer.NuevaNecesidadScreen
import com.example.cropfly.ui.producer.ResultadosScreen
import com.example.cropfly.ui.provider.PerfilPrestadorScreen
import com.example.cropfly.ui.provider.SolicitudesScreen
import com.example.cropfly.viewmodel.AdminViewModel
import com.example.cropfly.viewmodel.AuthViewModel
import com.example.cropfly.viewmodel.CuentaViewModel
import com.example.cropfly.viewmodel.EstadoSesion
import com.example.cropfly.viewmodel.InicioProductorViewModel
import com.example.cropfly.viewmodel.NuevaNecesidadViewModel
import com.example.cropfly.viewmodel.PerfilPrestadorViewModel
import com.example.cropfly.viewmodel.ResultadosViewModel
import com.example.cropfly.viewmodel.SolicitudesViewModel

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

        composable(
            route = Screen.Resultados.route,
            arguments = listOf(
                navArgument(Screen.Resultados.ARGUMENTO_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entrada ->
            val usuario = usuarioActual ?: return@composable
            val idRecibido = entrada.arguments?.getLong(Screen.Resultados.ARGUMENTO_ID) ?: -1L
            val necesidadId = if (idRecibido >= 0L) idRecibido else null
            val viewModel: ResultadosViewModel = viewModel(factory = fabrica.resultados(usuario.id, necesidadId))
            ResultadosScreen(
                usuarioId = usuario.id,
                viewModel = viewModel,
                alElegirPestana = { irAPestana(it, usuario.rol) },
                alVolver = { navController.popBackStack() },
                alComparar = { navController.navigate(Screen.Comparar.route) },
                alRegistrarNecesidad = { navController.navigate(Screen.NuevaNecesidad.route) }
            )
        }
        composable(Screen.Comparar.route) { entrada ->
            val usuario = usuarioActual ?: return@composable
            // "Comparar" usa el mismo ViewModel que "Resultados" (así recuerda lo que se seleccionó).
            val entradaResultados = remember(entrada) {
                runCatching { navController.getBackStackEntry(Screen.Resultados.route) }.getOrNull()
            }
            if (entradaResultados == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }
            val viewModel: ResultadosViewModel = viewModel(
                viewModelStoreOwner = entradaResultados,
                factory = fabrica.resultados(usuario.id, null)
            )
            CompararScreen(
                usuarioId = usuario.id,
                viewModel = viewModel,
                alElegirPestana = { irAPestana(it, usuario.rol) },
                alVolver = { navController.popBackStack() }
            )
        }

        // ---------- Cuenta (los tres perfiles) ----------
        composable(Screen.Cuenta.route) {
            val usuario = usuarioActual ?: return@composable
            val viewModel: CuentaViewModel = viewModel(factory = fabrica.cuenta(usuario.id))
            CuentaScreen(
                usuarioId = usuario.id,
                rol = usuario.rol,
                viewModel = viewModel,
                alElegirPestana = { irAPestana(it, usuario.rol) },
                alCerrarSesion = cerrarSesion
            )
        }

        // ---------- Prestador ----------
        composable(Screen.Solicitudes.route) {
            val usuario = usuarioActual ?: return@composable
            val viewModel: SolicitudesViewModel = viewModel(factory = fabrica.solicitudes(usuario.id))
            SolicitudesScreen(
                usuarioId = usuario.id,
                viewModel = viewModel,
                alElegirPestana = { irAPestana(it, usuario.rol) }
            )
        }
        composable(Screen.MiPerfil.route) {
            val usuario = usuarioActual ?: return@composable
            val viewModel: PerfilPrestadorViewModel = viewModel(factory = fabrica.perfilPrestador(usuario.id))
            PerfilPrestadorScreen(
                usuarioId = usuario.id,
                viewModel = viewModel,
                alElegirPestana = { irAPestana(it, usuario.rol) }
            )
        }

        // ---------- Administrador ----------
        composable(Screen.PanelAdmin.route) {
            val usuario = usuarioActual ?: return@composable
            val viewModel: AdminViewModel = viewModel(factory = fabrica.admin())
            PanelAdminScreen(
                usuarioId = usuario.id,
                viewModel = viewModel,
                alElegirPestana = { irAPestana(it, usuario.rol) }
            )
        }
    }
}
