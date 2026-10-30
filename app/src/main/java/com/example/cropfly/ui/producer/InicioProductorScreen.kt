package com.example.cropfly.ui.producer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.cropfly.model.Fechas
import com.example.cropfly.model.Roles
import com.example.cropfly.ui.components.Estructura
import com.example.cropfly.ui.components.InfoConIcono
import com.example.cropfly.ui.components.LogoDron
import com.example.cropfly.ui.components.Pildora
import com.example.cropfly.ui.components.TarjetaBlanca
import com.example.cropfly.ui.navigation.PestanaInferior
import com.example.cropfly.ui.theme.AccentGreen
import com.example.cropfly.ui.theme.BadgeOrange
import com.example.cropfly.ui.theme.ChipBackground
import com.example.cropfly.ui.theme.DarkGreen
import com.example.cropfly.ui.theme.ErrorRed
import com.example.cropfly.ui.theme.FieldBorder
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextDark
import com.example.cropfly.ui.theme.TextMuted
import com.example.cropfly.viewmodel.InicioProductorViewModel
import com.example.cropfly.viewmodel.NecesidadConResultados

/** Inicio del productor: saludo, resumen y la lista de sus necesidades. */
@Composable
fun InicioProductorScreen(
    usuarioId: Long,
    viewModel: InicioProductorViewModel,
    alElegirPestana: (PestanaInferior) -> Unit,
    alRegistrarNecesidad: () -> Unit,
    alVerResultados: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val necesidadPorEliminar by viewModel.necesidadPorEliminar.collectAsState()

    Estructura(
        titulo = "Inicio",
        usuarioId = usuarioId,
        pestanas = PestanaInferior.paraRol(Roles.PRODUCTOR),
        pestanaSeleccionada = PestanaInferior.INICIO,
        alElegirPestana = alElegirPestana
    ) { margenes ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(margenes),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { TarjetaDeBienvenida(alRegistrarNecesidad) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaDeCifra(estado.necesidades.size, "Necesidades", Modifier.weight(1f))
                    TarjetaDeCifra(estado.conResultados, "Con resultados", Modifier.weight(1f))
                    TarjetaDeCifra(estado.intereses, "Intereses", Modifier.weight(1f))
                }
            }
            item {
                Text(
                    text = "MIS NECESIDADES",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkGreen,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (estado.necesidades.isEmpty()) {
                item {
                    TarjetaBlanca {
                        Text("Aún no registras necesidades", style = MaterialTheme.typography.titleMedium, color = TextDark)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Toca \"Registrar necesidad\" para encontrar prestadores compatibles.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            items(estado.necesidades, key = { it.necesidad.id }) { elemento ->
                TarjetaDeNecesidad(
                    elemento = elemento,
                    alVerResultados = { alVerResultados(elemento.necesidad.id) },
                    alEliminar = { viewModel.pedirEliminar(elemento.necesidad) }
                )
            }
        }
    }

    val pendiente = necesidadPorEliminar
    if (pendiente != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarEliminar,
            title = { Text("Eliminar necesidad") },
            text = { Text("Se eliminará \"${pendiente.servicio}\" en ${pendiente.zona} junto con los intereses enviados.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarEliminar) { Text("Eliminar", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarEliminar) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TarjetaDeBienvenida(alRegistrarNecesidad: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(DarkGreen, Color(0xFF1B4D33))))
    ) {
        IlustracionDeCampo(modifier = Modifier.matchParentSize())
        Column(modifier = Modifier.padding(22.dp)) {
            Text("HOLA, PRODUCTOR", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(
                "Encuentra quién puede\ntrabajar tu campo.",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = alRegistrarNecesidad,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = TextDark)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Registrar necesidad", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Dibujo decorativo de la tarjeta: sol, dron y surcos del campo. */
@Composable
private fun IlustracionDeCampo(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.TopEnd) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trazo = Stroke(width = 3f)
            for (indice in 0 until 3) {
                val camino = Path().apply {
                    moveTo(size.width * 0.45f, size.height * (0.95f + indice * 0.12f))
                    quadraticTo(
                        size.width * 0.75f, size.height * (0.55f + indice * 0.12f),
                        size.width, size.height * (0.62f + indice * 0.12f)
                    )
                }
                drawPath(camino, AccentGreen.copy(alpha = 0.35f), style = trazo)
            }
            drawCircle(BadgeOrange, radius = 14.dp.toPx(), center = Offset(size.width - 20.dp.toPx(), 36.dp.toPx()))
        }
        LogoDron(
            tamano = 64.dp,
            modifier = Modifier.padding(top = 30.dp, end = 54.dp)
        )
    }
}

@Composable
private fun TarjetaDeCifra(cifra: Int, etiqueta: String, modifier: Modifier = Modifier) {
    TarjetaBlanca(modifier = modifier) {
        Text("$cifra", style = MaterialTheme.typography.headlineLarge, color = DarkGreen)
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = TextMuted, minLines = 2)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaDeNecesidad(
    elemento: NecesidadConResultados,
    alVerResultados: () -> Unit,
    alEliminar: () -> Unit
) {
    val necesidad = elemento.necesidad
    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                necesidad.servicio,
                style = MaterialTheme.typography.titleMedium,
                color = TextDark,
                modifier = Modifier.weight(1f)
            )
            if (necesidad.esEjemplo) {
                Pildora("Ejemplo", fondo = ChipBackground, colorTexto = TextMuted)
            }
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            InfoConIcono(Icons.Filled.Eco, necesidad.cultivo)
            InfoConIcono(Icons.Filled.Layers, "${necesidad.superficieHa} ha")
            InfoConIcono(Icons.Filled.LocationOn, necesidad.zona)
            InfoConIcono(Icons.Outlined.CalendarToday, Fechas.rango(necesidad.desde, necesidad.hasta))
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pildora(
                texto = if (elemento.compatibles == 1) "1 prestador compatible" else "${elemento.compatibles} prestadores compatibles",
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChipBackground)
                    .border(1.dp, FieldBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = alEliminar),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar necesidad", tint = TextDark)
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = alVerResultados,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Ver resultados", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
