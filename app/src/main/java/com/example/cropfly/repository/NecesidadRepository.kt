package com.example.cropfly.repository

import com.example.cropfly.model.Necesidad
import com.example.cropfly.model.dao.NecesidadDao
import com.example.cropfly.network.CropFlyApi
import com.example.cropfly.network.aDto
import com.example.cropfly.network.llamarListando
import com.example.cropfly.network.llamarSinFallar
import kotlinx.coroutines.flow.Flow

/**
 * Necesidades de servicio que registran los productores.
 * Se guardan siempre en el teléfono (Room) y además se publican en la API; si no hay internet, la app sigue funcionando.
 */
class NecesidadRepository(
    private val necesidadDao: NecesidadDao,
    private val api: CropFlyApi
) {
    fun observarPorProductor(productorId: Long): Flow<List<Necesidad>> =
        necesidadDao.observarPorProductor(productorId)

    fun observarTodas(): Flow<List<Necesidad>> = necesidadDao.observarTodas()

    suspend fun buscarPorId(id: Long): Necesidad? = necesidadDao.buscarPorId(id)

    /** Guarda la necesidad en el teléfono y la publica en la API (POST /necesidades). Devuelve su id local. */
    suspend fun crear(necesidad: Necesidad): Long {
        val idLocal = necesidadDao.insertar(necesidad)
        val publicada = llamarSinFallar { api.crearNecesidad(necesidad.aDto()) }
        publicada?.id?.let { idEnLaNube -> necesidadDao.asignarRemoteId(idLocal, idEnLaNube) }
        return idLocal
    }

    /** Borra la necesidad del teléfono y de la API (DELETE /necesidades/{id}). */
    suspend fun eliminar(id: Long) {
        val idEnLaNube = necesidadDao.buscarPorId(id)?.remoteId
        necesidadDao.eliminar(id)
        if (idEnLaNube != null) llamarSinFallar { api.eliminarNecesidad(idEnLaNube) }
    }

    /**
     * Cuántas necesidades de este productor hay publicadas en la API (GET /necesidades?productorId=).
     * Devuelve null si no se pudo consultar (sin conexión).
     */
    suspend fun contarPublicadas(productorId: Long): Int? =
        llamarListando { api.listarNecesidades(productorId) }
            ?.count { it.productorId == productorId }
}
