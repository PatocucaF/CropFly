package com.example.cropfly.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.cropfly.util.Notificador

/**
 * Devuelve una función "avisar(título, mensaje)" que lanza una notificación del sistema.
 * Si falta el permiso (Android 13+), lo pide primero y muestra el aviso solo si el usuario lo acepta.
 */
@Composable
fun rememberAvisadorNativo(): (String, String) -> Unit {
    val contexto = LocalContext.current
    var pendiente by remember { mutableStateOf<Pair<String, String>?>(null) }

    val lanzadorDePermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        val aviso = pendiente
        if (concedido && aviso != null) Notificador.mostrar(contexto, aviso.first, aviso.second)
        pendiente = null
    }

    return { titulo, mensaje ->
        if (Notificador.permisoConcedido(contexto)) {
            Notificador.mostrar(contexto, titulo, mensaje)
        } else {
            pendiente = titulo to mensaje
            lanzadorDePermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
