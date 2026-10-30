package com.example.cropfly.repository

import com.example.cropfly.model.Necesidad
import com.example.cropfly.model.dao.NecesidadDao
import kotlinx.coroutines.flow.Flow

/** Necesidades de servicio que registran los productores. */
class NecesidadRepository(private val necesidadDao: NecesidadDao) {
    fun observarPorProductor(productorId: Long): Flow<List<Necesidad>> =
        necesidadDao.observarPorProductor(productorId)

    fun observarTodas(): Flow<List<Necesidad>> = necesidadDao.observarTodas()

    suspend fun buscarPorId(id: Long): Necesidad? = necesidadDao.buscarPorId(id)

    /** Guarda la necesidad y devuelve su id nuevo. */
    suspend fun crear(necesidad: Necesidad): Long = necesidadDao.insertar(necesidad)

    suspend fun eliminar(id: Long) = necesidadDao.eliminar(id)
}
