package com.example.cropfly.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cropfly.ui.theme.ChipBackground
import com.example.cropfly.ui.theme.FieldBorder
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextDark
import com.example.cropfly.ui.theme.TextMuted

/** Selector de dos o más opciones en una sola barra (ej: "Ingresar" | "Crear cuenta"). El color cambia con animación. */
@Composable
fun SelectorSegmentado(
    opciones: List<String>,
    seleccionada: Int,
    alElegir: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ChipBackground)
            .padding(5.dp)
    ) {
        opciones.forEachIndexed { indice, texto ->
            val fondo by animateColorAsState(
                targetValue = if (indice == seleccionada) PrimaryGreen else Color.Transparent,
                label = "fondoSegmento"
            )
            val letra by animateColorAsState(
                targetValue = if (indice == seleccionada) Color.White else TextMuted,
                label = "letraSegmento"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(fondo)
                    .clickable { alElegir(indice) }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(texto, color = letra, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Píldora que se puede marcar (servicios, zonas, cultivos) o tocar (cuentas demo, filtros). */
@Composable
fun ChipSeleccion(
    texto: String,
    seleccionado: Boolean,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fondo by animateColorAsState(
        targetValue = if (seleccionado) PrimaryGreen else Color.White,
        label = "fondoChip"
    )
    val letra by animateColorAsState(
        targetValue = if (seleccionado) Color.White else TextDark,
        label = "letraChip"
    )
    val forma = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(forma)
            .background(fondo)
            .border(BorderStroke(1.dp, if (seleccionado) PrimaryGreen else FieldBorder), forma)
            .clickable(onClick = alPulsar)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, color = letra, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}
