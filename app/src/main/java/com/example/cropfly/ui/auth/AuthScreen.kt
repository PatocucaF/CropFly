package com.example.cropfly.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cropfly.model.Roles
import com.example.cropfly.model.Usuario
import com.example.cropfly.ui.components.BotonPrimario
import com.example.cropfly.ui.components.CampoTexto
import com.example.cropfly.ui.components.ChipSeleccion
import com.example.cropfly.ui.components.EtiquetaCampo
import com.example.cropfly.ui.components.MarcaCropFly
import com.example.cropfly.ui.components.SelectorSegmentado
import com.example.cropfly.ui.components.TextoDeError
import com.example.cropfly.ui.theme.BackgroundLight
import com.example.cropfly.ui.theme.DarkGreen
import com.example.cropfly.ui.theme.ErrorRed
import com.example.cropfly.ui.theme.FieldBorder
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextDark
import com.example.cropfly.ui.theme.TextMuted
import com.example.cropfly.viewmodel.AuthViewModel
import com.example.cropfly.viewmodel.EstadoSesion
import com.example.cropfly.viewmodel.ModoAuth


@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    estadoSesion: EstadoSesion,
    alIngresar: (Usuario) -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    // Cuando el ViewModel deja la sesión abierta (login o registro correcto) se avanza al inicio del perfil.
    LaunchedEffect(estadoSesion) {
        if (estadoSesion is EstadoSesion.Activa) alIngresar(estadoSesion.usuario)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundLight)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        CabeceraAuth()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-44).dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                SelectorSegmentado(
                    opciones = listOf("Ingresar", "Crear cuenta"),
                    seleccionada = if (estado.modo == ModoAuth.INGRESAR) 0 else 1,
                    alElegir = { indice ->
                        viewModel.cambiarModo(if (indice == 0) ModoAuth.INGRESAR else ModoAuth.CREAR_CUENTA)
                    }
                )
                Spacer(Modifier.height(18.dp))
                AnimatedContent(targetState = estado.modo, label = "formularioAuth") { modo ->
                    if (modo == ModoAuth.INGRESAR) {
                        FormularioIngreso(viewModel)
                    } else {
                        FormularioRegistro(viewModel)
                    }
                }
                Spacer(Modifier.height(22.dp))
                SeparadorPunteado()
                Spacer(Modifier.height(14.dp))
                CuentasDemo(alElegir = viewModel::usarCuentaDemo)
            }
        }
        Spacer(Modifier.height(8.dp).navigationBarsPadding())
    }
}

/** Parte verde de arriba: logo, titular y bajada. */
@Composable
private fun CabeceraAuth() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(DarkGreen, PrimaryGreen)))
            .drawBehind {
                // Rayas diagonales suaves del fondo, como en el mockup.
                val separacion = 36.dp.toPx()
                var x = -size.height
                while (x < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(x, size.height),
                        end = Offset(x + size.height, 0f),
                        strokeWidth = 2f
                    )
                    x += separacion
                }
            }
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 72.dp)
    ) {
        MarcaCropFly()
        Spacer(Modifier.height(28.dp))
        Text(
            text = "CONECTA TU CAMPO CON EL PRESTADOR CORRECTO",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Compara servicios de dron y robot agrícola. MVP académico con datos ficticios.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun FormularioIngreso(viewModel: AuthViewModel) {
    val estado by viewModel.estado.collectAsState()
    val errores = estado.errores

    Column {
        CampoTexto(
            valor = estado.correo,
            alCambiar = viewModel::onCorreoChange,
            etiqueta = "Correo",
            placeholder = "productor@demo.cl",
            icono = Icons.Outlined.Email,
            error = errores.correo,
            teclado = KeyboardType.Email
        )
        Spacer(Modifier.height(14.dp))
        CampoTexto(
            valor = estado.clave,
            alCambiar = viewModel::onClaveChange,
            etiqueta = "Contraseña",
            placeholder = "Tu contraseña",
            icono = Icons.Filled.Lock,
            error = errores.clave,
            esClave = true
        )
        ErrorGeneral(errores.general)
        Spacer(Modifier.height(18.dp))
        BotonPrimario("Ingresar", viewModel::ingresar, cargando = estado.cargando)
    }
}

@Composable
private fun FormularioRegistro(viewModel: AuthViewModel) {
    val estado by viewModel.estado.collectAsState()
    val errores = estado.errores

    Column {
        CampoTexto(
            valor = estado.nombre,
            alCambiar = viewModel::onNombreChange,
            etiqueta = "Nombre o alias ficticio",
            placeholder = "Productor Los Aromos",
            icono = Icons.Filled.Person,
            error = errores.nombre
        )
        Spacer(Modifier.height(14.dp))
        CampoTexto(
            valor = estado.correo,
            alCambiar = viewModel::onCorreoChange,
            etiqueta = "Correo",
            placeholder = "nombre@dominio.cl",
            icono = Icons.Outlined.Email,
            error = errores.correo,
            teclado = KeyboardType.Email
        )
        Spacer(Modifier.height(14.dp))
        EtiquetaCampo("Soy")
        SelectorSegmentado(
            opciones = listOf("Productor", "Prestador"),
            seleccionada = if (estado.rol == Roles.PRODUCTOR) 0 else 1,
            alElegir = { indice ->
                viewModel.onRolChange(if (indice == 0) Roles.PRODUCTOR else Roles.PRESTADOR)
            }
        )
        Spacer(Modifier.height(14.dp))
        CampoTexto(
            valor = estado.clave,
            alCambiar = viewModel::onClaveChange,
            etiqueta = "Contraseña",
            placeholder = "Mínimo 8, letras y números",
            icono = Icons.Filled.Lock,
            error = errores.clave,
            esClave = true
        )
        Spacer(Modifier.height(14.dp))
        CampoTexto(
            valor = estado.repetirClave,
            alCambiar = viewModel::onRepetirClaveChange,
            etiqueta = "Repetir contraseña",
            icono = Icons.Filled.Lock,
            error = errores.repetirClave,
            esClave = true
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = estado.aceptaTerminos,
                onCheckedChange = viewModel::onAceptaTerminosChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryGreen,
                    uncheckedColor = if (errores.terminos != null) ErrorRed else FieldBorder
                )
            )
            Text(
                text = "Entiendo que esta app usa solo datos ficticios.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDark
            )
        }
        TextoDeError(errores.terminos)
        ErrorGeneral(errores.general)
        Spacer(Modifier.height(18.dp))
        BotonPrimario("Crear cuenta", viewModel::registrar, cargando = estado.cargando)
    }
}

/** Mensaje rojo para errores que no son de un campo en particular (ej: "Correo o contraseña incorrectos"). */
@Composable
private fun ErrorGeneral(mensaje: String?) {
    AnimatedVisibility(visible = mensaje != null) {
        Text(
            text = mensaje.orEmpty(),
            color = ErrorRed,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun SeparadorPunteado() {
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = FieldBorder,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
        )
    }
}

/** Botones para rellenar el formulario con una cuenta de prueba. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CuentasDemo(alElegir: (String) -> Unit) {
    Text(
        text = "CUENTAS DEMO",
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted
    )
    Spacer(Modifier.height(10.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChipSeleccion("Productor", seleccionado = false, alPulsar = { alElegir(Roles.PRODUCTOR) })
        ChipSeleccion("Prestador", seleccionado = false, alPulsar = { alElegir(Roles.PRESTADOR) })
        ChipSeleccion("Admin", seleccionado = false, alPulsar = { alElegir(Roles.ADMIN) })
    }
}
