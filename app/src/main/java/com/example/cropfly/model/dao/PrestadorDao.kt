package com.example.cropfly.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cropfly.model.Prestador
import kotlinx.coroutines.flow.Flow

@Dao
interface PrestadorDao {
    @Insert
    suspend fun insertar(prestador: Prestador): Long

    @Update
    suspend fun actualizar(prestador: Prestador)

    @Query("DELETE FROM prestadores WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query("DELETE FROM prestadores")
    suspend fun eliminarTodos()

    @Query("UPDATE prestadores SET activo = :activo WHERE id = :id")
    suspend fun cambiarActivo(id: Long, activo: Boolean)

    @Query("SELECT * FROM prestadores WHERE id = :id")
    suspend fun buscarPorId(id: Long): Prestador?

    @Query("SELECT * FROM prestadores WHERE usuarioId = :usuarioId LIMIT 1")
    fun observarPorUsuario(usuarioId: Long): Flow<Prestador?>

    @Query("SELECT * FROM prestadores ORDER BY id")
    fun observarTodos(): Flow<List<Prestador>>
}
