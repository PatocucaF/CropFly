package com.example.cropfly.ui.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cropfly.model.Usuario
import com.example.cropfly.ui.components.LogoDron
import com.example.cropfly.ui.theme.AccentGreen
import com.example.cropfly.ui.theme.DarkGreen
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.viewmodel.EstadoSesion
import kotlinx.coroutines.delay

private const val DURACION_SPLASH_MS = 1_800L

@Composable
fun SplashScreen(
    estadoSesion: EstadoSesion,
    alTerminar: (Usuario?) -> Unit
) {
    var pasoElTiempo by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    val transparencia by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "transparenciaSplash"
    )
    val escala by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(durationMillis = 900),
        label = "escalaSplash"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(DURACION_SPLASH_MS)
        pasoElTiempo = true
    }

    LaunchedEffect(pasoElTiempo, estadoSesion) {
        if (pasoElTiempo) {
            when (estadoSesion) {
                is EstadoSesion.Activa -> alTerminar(estadoSesion.usuario)
                is EstadoSesion.SinSesion -> alTerminar(null)
                is EstadoSesion.Cargando -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryGreen, DarkGreen, PrimaryGreen))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(transparencia)
                .scale(escala)
                .padding(horizontal = 24.dp)
        ) {
            LogoDron(tamano = 120.dp, colorCuerpo = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.height(24.dp))
            Row {
                Text("Crop", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                Text("Fly", color = AccentGreen, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Servicios agrícolas con dron y robot, a un toque.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
