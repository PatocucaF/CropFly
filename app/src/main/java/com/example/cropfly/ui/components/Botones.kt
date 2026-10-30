package com.example.cropfly.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cropfly.ui.theme.PrimaryGreen

private val FormaBoton = RoundedCornerShape(26.dp)

/** Botón verde sólido. Si [cargando] es true muestra una ruedita en vez del texto. */
@Composable
fun BotonPrimario(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    icono: ImageVector? = null,
    cargando: Boolean = false,
    habilitado: Boolean = true,
    alto: Int = 54
) {
    Button(
        onClick = alPulsar,
        enabled = habilitado && !cargando,
        modifier = modifier.height(alto.dp),
        shape = FormaBoton,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen,
            contentColor = Color.White,
            disabledContainerColor = PrimaryGreen.copy(alpha = 0.45f),
            disabledContentColor = Color.White
        )
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            ContenidoDeBoton(texto, icono)
        }
    }
}

/** Botón con borde verde y fondo blanco. */
@Composable
fun BotonSecundario(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    icono: ImageVector? = null,
    alto: Int = 54
) {
    OutlinedButton(
        onClick = alPulsar,
        modifier = modifier.height(alto.dp),
        shape = FormaBoton,
        border = BorderStroke(1.5.dp, PrimaryGreen),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = PrimaryGreen)
    ) {
        ContenidoDeBoton(texto, icono)
    }
}

@Composable
private fun ContenidoDeBoton(texto: String, icono: ImageVector?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, fontWeight = FontWeight.SemiBold)
    }
}
