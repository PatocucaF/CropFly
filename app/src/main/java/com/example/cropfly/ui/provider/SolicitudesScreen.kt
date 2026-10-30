package com.example.cropfly.ui.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.MaterialTheme
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
import com.example.cropfly.model.Fechas
import com.example.cropfly.model.Roles
import com.example.cropfly.model.SolicitudRecibida
import com.example.cropfly.ui.components.BotonPrimario
import com.example.cropfly.ui.components.Estructura
import com.example.cropfly.ui.components.InfoConIcono
import com.example.cropfly.ui.components.Pildora
import com.example.cropfly.ui.components.TarjetaBlanca
import com.example.cropfly.ui.navigation.PestanaInferior
import com.example.cropfly.ui.theme.AccentGreen
import com.example.cropfly.ui.theme.ChipBackground
import com.example.cropfly.ui.theme.DarkGreen
import com.example.cropfly.ui.theme.LightGreenContainer
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextDark
import com.example.cropfly.ui.theme.TextMuted
import com.example.cropfly.ui.theme.WarningContainer
import com.example.cropfly.ui.theme.WarningText
import com.example.cropfly.viewmodel.SolicitudesViewModel

/** Pantalla principal del prestador: su resumen y los intereses que le enviaron los productores. */
@Composable
fun SolicitudesScreen(
    usuarioId: Long,
    viewModel: SolicitudesViewModel,
    alElegirPestana: (PestanaInferior) -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    Estructura(
        titulo = "Solicitudes",
        usuarioId = usuarioId,
        pestanas = PestanaInferior.paraRol(Roles.PRESTADOR),
        pestanaSeleccionada = PestanaInferior.SOLICITUDES,
        alElegirPestana = alElegirPestana
    ) { margenes ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(margenes),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                val prestador = estado.prestador
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(DarkGreen, Color(0xFF1B4D33))))
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            (prestador?.nombre ?: "Mi empresa").uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.height(6.dp))
                        if (prestador != null && prestador.perfilCompleto) {
                            Text(
                                "${prestador.capacidadHaDia} ha/día · ${prestador.experienciaAnios} años",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            Text(
                                "Completa tu perfil para aparecer en los resultados.",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        if (estado.visibleParaProductores) {
                            Pildora("Visible para productores", fondo = LightGreenContainer, colorTexto = PrimaryGreen)
                        } else {
                            Pildora("No visible para productores", fondo = WarningContainer, colorTexto = WarningText)
                        }
                    }
                }
            }
            item {
                Text(
                    "SOLICITUDES RECIBIDAS",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkGreen,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (estado.solicitudes.isEmpty()) {
                item {
                    TarjetaBlanca {
                        Text("Aún no tienes solicitudes", style = MaterialTheme.typography.titleMedium, color = TextDark)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Cuando un productor manifieste interés en tu perfil, aparecerá aquí.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            items(estado.solicitudes, key = { it.interes.id }) { solicitud ->
                TarjetaDeSolicitud(solicitud, alMarcarVista = { viewModel.marcarComoVista(solicitud.interes.id) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaDeSolicitud(solicitud: SolicitudRecibida, alMarcarVista: () -> Unit) {
    val necesidad = solicitud.necesidad
    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                necesidad.servicio,
                style = MaterialTheme.typography.titleMedium,
                color = TextDark,
                modifier = Modifier.weight(1f)
            )
            if (solicitud.interes.vista) {
                Pildora("Vista", fondo = ChipBackground, colorTexto = TextMuted, icono = Icons.Filled.Check)
            } else {
                Pildora("Nueva", fondo = WarningContainer, colorTexto = WarningText)
            }
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            InfoConIcono(Icons.Filled.Eco, necesidad.cultivo)
            InfoConIcono(Icons.Filled.Layers, "${necesidad.superficieHa} ha")
            InfoConIcono(Icons.Filled.LocationOn, necesidad.zona)
            InfoConIcono(Icons.Outlined.CalendarToday, Fechas.rango(necesidad.desde, necesidad.hasta))
        }
        if (!solicitud.interes.vista) {
            Spacer(Modifier.height(14.dp))
            BotonPrimario("Marcar como vista", alMarcarVista, modifier = Modifier, alto = 46)
        }
    }
}
