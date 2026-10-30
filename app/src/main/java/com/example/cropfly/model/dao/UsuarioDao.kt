package com.example.cropfly.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cropfly.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    /** La sesión guardada: el usuario que quedó con sesión abierta la última vez. */
    @Query("SELECT * FROM usuarios WHERE sesionActiva = 1 AND activo = 1 LIMIT 1")
    suspend fun buscarSesionActiva(): Usuario?

    @Query("UPDATE usuarios SET sesionActiva = 0")
    suspend fun cerrarTodasLasSesiones()

    @Query("UPDATE usuarios SET sesionActiva = 1 WHERE id = :id")
    suspend fun activarSesion(id: Long)

    @Query("UPDATE usuarios SET activo = :activo WHERE id = :id")
    suspend fun cambiarActivo(id: Long, activo: Boolean)

    @Query("UPDATE usuarios SET activo = 1")
    suspend fun reactivarTodos()

    @Query("UPDATE usuarios SET ubicacionPermitida = :permitida WHERE id = :id")
    suspend fun cambiarUbicacionPermitida(id: Long, permitida: Boolean)

    @Query("SELECT * FROM usuarios WHERE rol = :rol")
    suspend fun listarPorRol(rol: String): List<Usuario>

    @Query("SELECT * FROM usuarios ORDER BY id")
    fun observarTodos(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observarPorId(id: Long): Flow<Usuario?>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}
