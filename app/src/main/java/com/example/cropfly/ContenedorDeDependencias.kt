package com.example.cropfly

import android.content.Context
import com.example.cropfly.model.CropFlyDatabase
import com.example.cropfly.network.ClienteApi
import com.example.cropfly.repository.DatosDePruebaRepository
import com.example.cropfly.repository.InteresRepository
import com.example.cropfly.repository.NecesidadRepository
import com.example.cropfly.repository.NotificacionRepository
import com.example.cropfly.repository.PrestadorRepository
import com.example.cropfly.repository.UsuarioRepository

/**
 * Aquí se crean una sola vez la base de datos, el cliente de la API y los repositorios.
 * Los ViewModels los reciben desde aquí (así no crean nada por su cuenta y son fáciles de probar).
 */
class ContenedorDeDependencias(contexto: Context) {
    private val baseDeDatos = CropFlyDatabase.crear(contexto)

    private val api = ClienteApi.api

    val usuarioRepository = UsuarioRepository(baseDeDatos.usuarioDao(), baseDeDatos.prestadorDao())
    val prestadorRepository = PrestadorRepository(baseDeDatos.prestadorDao(), api)
    val necesidadRepository = NecesidadRepository(baseDeDatos.necesidadDao(), api)
    val interesRepository = InteresRepository(baseDeDatos.interesDao(), baseDeDatos.notificacionDao())
    val notificacionRepository = NotificacionRepository(baseDeDatos.notificacionDao())
    val datosDePruebaRepository = DatosDePruebaRepository(baseDeDatos)
}
