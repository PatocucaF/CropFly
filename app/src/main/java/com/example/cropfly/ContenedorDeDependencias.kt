package com.example.cropfly

import android.content.Context
import com.example.cropfly.model.CropFlyDatabase
import com.example.cropfly.repository.DatosDePruebaRepository
import com.example.cropfly.repository.InteresRepository
import com.example.cropfly.repository.NecesidadRepository
import com.example.cropfly.repository.NotificacionRepository
import com.example.cropfly.repository.PrestadorRepository
import com.example.cropfly.repository.UsuarioRepository

/**
 * Aquí se crean una sola vez la base de datos y los repositorios.
 * Los ViewModels los reciben desde aquí (así no crean nada por su cuenta y son fáciles de probar).
 */
class ContenedorDeDependencias(contexto: Context) {
    private val baseDeDatos = CropFlyDatabase.crear(contexto)

    val usuarioRepository = UsuarioRepository(baseDeDatos.usuarioDao(), baseDeDatos.prestadorDao())
    val prestadorRepository = PrestadorRepository(baseDeDatos.prestadorDao())
    val necesidadRepository = NecesidadRepository(baseDeDatos.necesidadDao())
    val interesRepository = InteresRepository(baseDeDatos.interesDao(), baseDeDatos.notificacionDao())
    val notificacionRepository = NotificacionRepository(baseDeDatos.notificacionDao())
    val datosDePruebaRepository = DatosDePruebaRepository(baseDeDatos)
}
