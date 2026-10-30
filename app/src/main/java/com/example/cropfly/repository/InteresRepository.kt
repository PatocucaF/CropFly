package com.example.cropfly.repository

import com.example.cropfly.model.Interes
import com.example.cropfly.model.Necesidad
import com.example.cropfly.model.Notificacion
import com.example.cropfly.model.Prestador
import com.example.cropfly.model.SolicitudRecibida
import com.example.cropfly.model.dao.InteresDao
import com.example.cropfly.model.dao.NotificacionDao
import kotlinx.coroutines.flow.Flow

/** "Me interesa": la vinculación simulada entre productor y prestador. */
class InteresRepository(
    private val interesDao: InteresDao,
    private val notificacionDao: NotificacionDao
) {
    /**
     * Registra el interés y avisa a las dos partes (campanita).
     * Devuelve false si ya se había enviado antes a ese prestador.
     */
    suspend fun enviar(necesidad: Necesidad, prestador: Prestador, productorId: Long): Boolean {
        val id = interesDao.insertar(
            Interes(necesidadId = necesidad.id, prestadorId = prestador.id, productorId = productorId)
        )
        if (id == -1L) return false

        notificacionDao.insertar(
            Notificacion(usuarioId = productorId, mensaje = "Enviaste tu interés a ${prestador.nombre}")
        )
        val usuarioDelPrestador = prestador.usuarioId
        if (usuarioDelPrestador != null) {
            notificacionDao.insertar(
                Notificacion(
                    usuarioId = usuarioDelPrestador,
                    mensaje = "Nueva solicitud: ${necesidad.servicio} en ${necesidad.zona}"
                )
            )
        }
        return true
    }

    suspend fun marcarVista(interesId: Long) = interesDao.marcarVista(interesId)

    fun observarPorProductor(productorId: Long): Flow<List<Interes>> =
        interesDao.observarPorProductor(productorId)

    fun observarPorNecesidad(necesidadId: Long): Flow<List<Interes>> =
        interesDao.observarPorNecesidad(necesidadId)

    fun observarRecibidos(prestadorId: Long): Flow<List<SolicitudRecibida>> =
        interesDao.observarRecibidos(prestadorId)
}
