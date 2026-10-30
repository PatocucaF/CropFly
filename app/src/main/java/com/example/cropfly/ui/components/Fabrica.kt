package com.example.cropfly.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.cropfly.CropFlyApp
import com.example.cropfly.viewmodel.FabricaDeViewModels

/** Entrega la fábrica de ViewModels a cualquier pantalla (sale del contenedor de la Application). */
@Composable
fun rememberFabrica(): FabricaDeViewModels {
    val aplicacion = LocalContext.current.applicationContext as CropFlyApp
    return remember { FabricaDeViewModels(aplicacion.contenedor) }
}
