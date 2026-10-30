package com.example.cropfly.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.cropfly.model.Interes
import com.example.cropfly.model.SolicitudRecibida
import kotlinx.coroutines.flow.Flow

@Dao
interface InteresDao {
    /** Devuelve -1 si ese prestador ya tenía interés enviado para esa necesidad. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(interes: Interes): Long

    @Query("DELETE FROM intereses")
    suspend fun eliminarTodos()

    @Query("UPDATE intereses SET vista = 1 WHERE id = :id")
    suspend fun marcarVista(id: Long)

    @Query("SELECT * FROM intereses WHERE productorId = :productorId")
    fun observarPorProductor(productorId: Long): Flow<List<Interes>>

    @Query("SELECT * FROM intereses WHERE necesidadId = :necesidadId")
    fun observarPorNecesidad(necesidadId: Long): Flow<List<Interes>>

    @Transaction
    @Query("SELECT * FROM intereses WHERE prestadorId = :prestadorId ORDER BY enviadoEn DESC, id DESC")
    fun observarRecibidos(prestadorId: Long): Flow<List<SolicitudRecibida>>
}
