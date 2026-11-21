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

    @Query("UPDATE prestadores SET remoteId = :remoteId WHERE id = :id")
    suspend fun asignarRemoteId(id: Long, remoteId: String)

    @Query("SELECT * FROM prestadores WHERE remoteId = :remoteId LIMIT 1")
    suspend fun buscarPorRemoteId(remoteId: String): Prestador?

    /** Sirve para reconocer un prestador de prueba que ya existe en la nube pero aún no tiene remoteId aquí. */
    @Query("SELECT * FROM prestadores WHERE remoteId IS NULL AND nombre = :nombre LIMIT 1")
    suspend fun buscarSinRemoteIdPorNombre(nombre: String): Prestador?

    @Query("SELECT * FROM prestadores WHERE remoteId IS NULL")
    suspend fun listarSinRemoteId(): List<Prestador>

    @Query("SELECT * FROM prestadores WHERE id = :id")
    suspend fun buscarPorId(id: Long): Prestador?

    @Query("SELECT * FROM prestadores WHERE usuarioId = :usuarioId LIMIT 1")
    fun observarPorUsuario(usuarioId: Long): Flow<Prestador?>

    @Query("SELECT * FROM prestadores ORDER BY id")
    fun observarTodos(): Flow<List<Prestador>>
}
