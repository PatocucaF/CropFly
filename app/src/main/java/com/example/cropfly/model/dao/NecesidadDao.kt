package com.example.cropfly.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cropfly.model.Necesidad
import kotlinx.coroutines.flow.Flow

@Dao
interface NecesidadDao {
    @Insert
    suspend fun insertar(necesidad: Necesidad): Long

    @Query("DELETE FROM necesidades WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query("DELETE FROM necesidades")
    suspend fun eliminarTodas()

    @Query("SELECT * FROM necesidades WHERE id = :id")
    suspend fun buscarPorId(id: Long): Necesidad?

    @Query("SELECT * FROM necesidades WHERE productorId = :productorId ORDER BY creadaEn DESC, id DESC")
    fun observarPorProductor(productorId: Long): Flow<List<Necesidad>>

    @Query("SELECT * FROM necesidades ORDER BY creadaEn DESC, id DESC")
    fun observarTodas(): Flow<List<Necesidad>>
}
