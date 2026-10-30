package com.example.cropfly.repository

import com.example.cropfly.model.Prestador
import com.example.cropfly.model.dao.PrestadorDao
import kotlinx.coroutines.flow.Flow

/** Catálogo de prestadores (los de prueba y los que se registran). */
class PrestadorRepository(private val prestadorDao: PrestadorDao) {
    fun observarTodos(): Flow<List<Prestador>> = prestadorDao.observarTodos()

    fun observarPorUsuario(usuarioId: Long): Flow<Prestador?> = prestadorDao.observarPorUsuario(usuarioId)

    suspend fun buscarPorId(id: Long): Prestador? = prestadorDao.buscarPorId(id)

    suspend fun agregar(prestador: Prestador): Long = prestadorDao.insertar(prestador)

    suspend fun actualizar(prestador: Prestador) = prestadorDao.actualizar(prestador)

    suspend fun eliminar(id: Long) = prestadorDao.eliminar(id)

    suspend fun cambiarActivo(id: Long, activo: Boolean) = prestadorDao.cambiarActivo(id, activo)
}
