package com.example.cropfly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cropfly.model.Fechas
import com.example.cropfly.ui.navigation.PestanaInferior
import com.example.cropfly.ui.theme.BackgroundLight
import com.example.cropfly.ui.theme.BadgeOrange
import com.example.cropfly.ui.theme.ChipBackground
import com.example.cropfly.ui.theme.DarkGreen
import com.example.cropfly.ui.theme.LightGreenContainer
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextDark
import com.example.cropfly.ui.theme.TextMuted
import com.example.cropfly.viewmodel.NotificacionesViewModel

/**
 * Marco común de las pantallas internas: barra verde arriba (logo o flecha atrás, título y campanita)
 * y barra de navegación abajo según el perfil.
 * Si [alVolver] es null se muestra el logo; si no, una flecha para volver.
 */
@Composable
fun Estructura(
    titulo: String,
    usuarioId: Long,
    pestanas: List<PestanaInferior>,
    pestanaSeleccionada: PestanaInferior?,
    alElegirPestana: (PestanaInferior) -> Unit,
    modifier: Modifier = Modifier,
    alVolver: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
    contenido: @Composable (PaddingValues) -> Unit
) {
    val fabrica = rememberFabrica()
    val notificacionesViewModel: NotificacionesViewModel = viewModel(
        key = "notificaciones-$usuarioId",
        factory = fabrica.notificaciones(usuarioId)
    )
    val notificaciones by notificacionesViewModel.estado.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = BackgroundLight,
        topBar = {
            BarraSuperior(
                titulo = titulo,
                sinLeer = notificaciones.sinLeer,
                alVolver = alVolver,
                alAbrirNotificaciones = notificacionesViewModel::abrirPanel
            )
        },
        bottomBar = {
            BarraInferior(pestanas, pestanaSeleccionada, alElegirPestana)
        },
        snackbarHost = {
            if (snackbarHostState != null) SnackbarHost(snackbarHostState)
        },
        content = contenido
    )

    if (notificaciones.panelAbierto) {
        HojaModal(alCerrar = notificacionesViewModel::cerrarPanel) {
            TituloDeModal("Notificaciones")
            Spacer(Modifier.height(12.dp))
            if (notificaciones.notificaciones.isEmpty()) {
                Text("Aún no tienes notificaciones.", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            }
            notificaciones.notificaciones.forEach { aviso ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = PrimaryGreen)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            aviso.mensaje,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextDark
                        )
                        Text(Fechas.corta(aviso.fecha), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            BotonPrimario("Listo", notificacionesViewModel::cerrarPanel)
        }
    }
}

@Composable
private fun BarraSuperior(
    titulo: String,
    sinLeer: Int,
    alVolver: (() -> Unit)?,
    alAbrirNotificaciones: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(DarkGreen, Color(0xFF1B4D33))))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonCuadrado(onClick = alVolver) {
                if (alVolver != null) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                } else {
                    LogoDron(tamano = 26.dp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = titulo.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            BotonCuadrado(onClick = alAbrirNotificaciones) {
                BadgedBox(
                    badge = {
                        if (sinLeer > 0) {
                            Badge(containerColor = BadgeOrange, contentColor = Color.White) { Text("$sinLeer") }
                        }
                    }
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones", tint = Color.White)
                }
            }
        }
    }
}

/** Cuadrito translúcido de la barra superior (flecha, logo o campanita). */
@Composable
private fun BotonCuadrado(onClick: (() -> Unit)?, contenido: @Composable () -> Unit) {
    val base = Modifier
        .size(44.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color.White.copy(alpha = 0.12f))
    Box(
        modifier = if (onClick != null) base.clickable(onClick = onClick) else base,
        contentAlignment = Alignment.Center
    ) {
        contenido()
    }
}

@Composable
private fun BarraInferior(
    pestanas: List<PestanaInferior>,
    seleccionada: PestanaInferior?,
    alElegir: (PestanaInferior) -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        pestanas.forEach { pestana ->
            NavigationBarItem(
                selected = pestana == seleccionada,
                onClick = { alElegir(pestana) },
                icon = { Icon(pestana.icono, contentDescription = pestana.etiqueta) },
                label = { Text(pestana.etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    indicatorColor = LightGreenContainer,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
