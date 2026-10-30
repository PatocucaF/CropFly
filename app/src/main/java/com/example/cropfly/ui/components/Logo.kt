package com.example.cropfly.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cropfly.ui.theme.AccentGreen

/** Dibuja el dron del logo (una X con cuatro hélices y un centro verde) usando solo código. */
@Composable
fun LogoDron(tamano: Dp, modifier: Modifier = Modifier, colorCuerpo: Color = Color.White) {
    Canvas(modifier = modifier.size(tamano)) {
        val centro = Offset(size.width / 2f, size.height / 2f)
        val brazo = size.width * 0.36f
        val grosor = size.width * 0.07f
        val radioHelice = size.width * 0.13f
        val diagonales = listOf(Offset(-1f, -1f), Offset(1f, -1f), Offset(-1f, 1f), Offset(1f, 1f))
        diagonales.forEach { direccion ->
            val punta = Offset(centro.x + direccion.x * brazo, centro.y + direccion.y * brazo)
            drawLine(colorCuerpo, centro, punta, strokeWidth = grosor, cap = StrokeCap.Round)
            drawCircle(colorCuerpo.copy(alpha = 0.55f), radius = radioHelice, center = punta)
        }
        drawCircle(colorCuerpo, radius = size.width * 0.15f, center = centro)
        drawCircle(AccentGreen, radius = size.width * 0.08f, center = centro)
    }
}

/** Dron + la palabra AGROENLACE (AGRO en el color indicado y ENLACE en verde limón). */
@Composable
fun MarcaAgroEnlace(
    modifier: Modifier = Modifier,
    tamanoLogo: Dp = 40.dp,
    tamanoTexto: Int = 22,
    colorTexto: Color = Color.White
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        LogoDron(tamano = tamanoLogo, colorCuerpo = colorTexto)
        Spacer(Modifier.width(10.dp))
        Text("AGRO", color = colorTexto, fontSize = tamanoTexto.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
        Text("ENLACE", color = AccentGreen, fontSize = tamanoTexto.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
    }
}
