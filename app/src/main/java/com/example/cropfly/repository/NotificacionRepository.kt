package com.example.cropfly.repository

import com.example.cropfly.model.Notificacion
import com.example.cropfly.model.dao.NotificacionDao
import kotlinx.coroutines.flow.Flow

/** Avisos de la campanita. */
class NotificacionRepository(private val notificacionDao: NotificacionDao) {
    fun observarPorUsuario(usuarioId: Long): Flow<List<Notificacion>> =
        notificacionDao.observarPorUsuario(usuarioId)

    suspend fun marcarTodasLeidas(usuarioId: Long) = notificacionDao.marcarTodasLeidas(usuarioId)
}
