package com.example.cropfly.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cropfly.model.Notificacion
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {
    @Insert
    suspend fun insertar(notificacion: Notificacion): Long

    @Query("DELETE FROM notificaciones")
    suspend fun eliminarTodas()

    @Query("UPDATE notificaciones SET leida = 1 WHERE usuarioId = :usuarioId")
    suspend fun marcarTodasLeidas(usuarioId: Long)

    @Query("SELECT * FROM notificaciones WHERE usuarioId = :usuarioId ORDER BY fecha DESC, id DESC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<Notificacion>>
}
