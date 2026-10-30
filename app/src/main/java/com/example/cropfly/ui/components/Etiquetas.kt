package com.example.cropfly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.cropfly.ui.theme.LightGreenContainer
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextMuted

/** Tarjeta blanca de esquinas redondeadas que se repite en casi todas las pantallas. */
@Composable
fun TarjetaBlanca(
    modifier: Modifier = Modifier.fillMaxWidth(),
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = contenido)
    }
}

/** Etiqueta redondeada de texto corto (ej: "Ejemplo", "Nueva", "MEJOR", "Zona"). */
@Composable
fun Pildora(
    texto: String,
    modifier: Modifier = Modifier,
    fondo: Color = LightGreenContainer,
    colorTexto: Color = PrimaryGreen,
    icono: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(texto, color = colorTexto, style = MaterialTheme.typography.bodySmall)
    }
}

/** Ícono pequeño seguido de un texto gris (ej: hoja + "Cerezo"). */
@Composable
fun InfoConIcono(icono: ImageVector, texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text(texto, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}
