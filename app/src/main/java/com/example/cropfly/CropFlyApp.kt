package com.example.cropfly

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Se crea apenas se abre la app, antes que cualquier pantalla. */
class CropFlyApp : Application() {
    lateinit var contenedor: ContenedorDeDependencias
        private set

    private val alcanceDeLaApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorDeDependencias(this)
        // Primera vez que se instala: carga las cuentas demo y el catálogo de prestadores.
        alcanceDeLaApp.launch { contenedor.datosDePruebaRepository.sembrarSiVacio() }
    }
}
