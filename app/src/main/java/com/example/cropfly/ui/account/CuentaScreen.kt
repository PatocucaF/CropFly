package com.example.cropfly.ui.account

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cropfly.model.Roles
import com.example.cropfly.model.Usuario
import com.example.cropfly.ui.components.BotonSecundario
import com.example.cropfly.ui.components.Estructura
import com.example.cropfly.ui.components.Pildora
import com.example.cropfly.ui.components.TarjetaBlanca
import com.example.cropfly.ui.navigation.PestanaInferior
import com.example.cropfly.ui.theme.ChipBackground
import com.example.cropfly.ui.theme.DarkGreen
import com.example.cropfly.ui.theme.FieldBorder
import com.example.cropfly.ui.theme.LightGreenContainer
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextDark
import com.example.cropfly.ui.theme.TextMuted
import com.example.cropfly.util.Notificador
import com.example.cropfly.viewmodel.CuentaViewModel

/** "Mi cuenta": la usan los tres perfiles. Muestra los datos, los permisos del dispositivo y permite cerrar sesión. */
@Composable
fun CuentaScreen(
    usuarioId: Long,
    rol: String,
    viewModel: CuentaViewModel,
    alElegirPestana: (PestanaInferior) -> Unit,
    alCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuario.collectAsState()

    Estructura(
        titulo = "Mi cuenta",
        usuarioId = usuarioId,
        pestanas = PestanaInferior.paraRol(rol),
        pestanaSeleccionada = PestanaInferior.CUENTA,
        alElegirPestana = alElegirPestana
    ) { margenes ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(margenes)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            usuario?.let { Encabezado(it) }
            Spacer(Modifier.height(24.dp))
            Text("PERMISOS DEL DISPOSITIVO", style = MaterialTheme.typography.titleLarge, color = DarkGreen)
            Spacer(Modifier.height(12.dp))
            PermisoDeNotificaciones()
            Spacer(Modifier.height(12.dp))
            PermisoDeUbicacion(
                permitida = usuario?.ubicacionPermitida == true,
                alCambiar = viewModel::cambiarUbicacionPermitida
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Todos los datos de esta maqueta son ficticios y se guardan solo en este dispositivo.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(20.dp))
            BotonSecundario("Cerrar sesión", alCerrarSesion, icono = Icons.AutoMirrored.Filled.Logout)
        }
    }
}

@Composable
private fun Encabezado(usuario: Usuario) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PrimaryGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(iniciales(usuario.nombre), color = Color.White, style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(usuario.nombre.uppercase(), style = MaterialTheme.typography.headlineMedium, color = DarkGreen)
            Text(usuario.correo, color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(6.dp))
            Pildora(Roles.etiqueta(usuario.rol), fondo = LightGreenContainer, colorTexto = PrimaryGreen)
        }
    }
}

private fun iniciales(nombre: String): String =
    nombre.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

/**
 * Recurso nativo: notificaciones. En Android 13 o superior el sistema pide permiso; si ya se dio,
 * apagar el interruptor lleva a los ajustes del teléfono (una app no puede quitarse el permiso sola).
 */
@Composable
private fun PermisoDeNotificaciones() {
    val contexto = LocalContext.current
    var concedido by remember { mutableStateOf(Notificador.permisoConcedido(contexto)) }
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { respuesta ->
        concedido = respuesta
    }

    FilaDePermiso(
        icono = Icons.Outlined.Notifications,
        titulo = "Notificaciones",
        detalle = if (concedido) "Permitidas" else "Sin configurar",
        activo = concedido,
        alCambiar = { quiereActivar ->
            if (quiereActivar) {
                lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                val ajustes = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
                contexto.startActivity(ajustes)
            }
        }
    )
}

@Composable
private fun PermisoDeUbicacion(permitida: Boolean, alCambiar: (Boolean) -> Unit) {
    FilaDePermiso(
        icono = Icons.Filled.LocationOn,
        titulo = "Ubicación",
        detalle = if (permitida) "Permitida (simulada)" else "No permitida",
        activo = permitida,
        alCambiar = alCambiar
    )
}

@Composable
private fun FilaDePermiso(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    activo: Boolean,
    alCambiar: (Boolean) -> Unit
) {
    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChipBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = PrimaryGreen)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = TextDark)
                Text(detalle, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
            Switch(
                checked = activo,
                onCheckedChange = alCambiar,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = PrimaryGreen,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = FieldBorder,
                    uncheckedThumbColor = Color.White,
                    uncheckedBorderColor = FieldBorder
                )
            )
        }
    }
}
