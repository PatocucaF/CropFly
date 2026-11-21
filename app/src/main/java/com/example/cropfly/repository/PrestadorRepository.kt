package com.example.cropfly.repository

import com.example.cropfly.model.Prestador
import com.example.cropfly.model.dao.PrestadorDao
import com.example.cropfly.network.CropFlyApi
import com.example.cropfly.network.PrestadorDto
import com.example.cropfly.network.aDto
import com.example.cropfly.network.aModelo
import com.example.cropfly.network.llamarListando
import com.example.cropfly.network.llamarSinFallar
import kotlinx.coroutines.flow.Flow

/**
 * Catálogo de prestadores (los de prueba y los que se registran).
 * Room es lo que ve la pantalla; la API es la copia compartida en la nube, y se sincronizan entre sí.
 */
class PrestadorRepository(
    private val prestadorDao: PrestadorDao,
    private val api: CropFlyApi
) {
    fun observarTodos(): Flow<List<Prestador>> = prestadorDao.observarTodos()

    fun observarPorUsuario(usuarioId: Long): Flow<Prestador?> = prestadorDao.observarPorUsuario(usuarioId)

    /** Guarda el prestador en el teléfono y lo publica en la API (POST /prestadores). */
    suspend fun agregar(prestador: Prestador): Long {
        val idLocal = prestadorDao.insertar(prestador)
        publicar(prestador.copy(id = idLocal))
        return idLocal
    }

    /** Guarda los cambios en el teléfono y, si el perfil está completo, también en la API (PUT /prestadores/{id}). */
    suspend fun actualizar(prestador: Prestador) {
        prestadorDao.actualizar(prestador)
        if (prestador.perfilCompleto) publicar(prestador)
    }

    /** Borra el prestador del teléfono y de la API (DELETE /prestadores/{id}). */
    suspend fun eliminar(id: Long) {
        val idEnLaNube = prestadorDao.buscarPorId(id)?.remoteId
        prestadorDao.eliminar(id)
        if (idEnLaNube != null) llamarSinFallar { api.eliminarPrestador(idEnLaNube) }
    }

    suspend fun cambiarActivo(id: Long, activo: Boolean) {
        prestadorDao.cambiarActivo(id, activo)
        val actualizado = prestadorDao.buscarPorId(id) ?: return
        if (actualizado.perfilCompleto) publicar(actualizado)
    }

    /**
     * Trae el catálogo de la API (GET /prestadores), lo guarda en Room y sube los prestadores
     * que existen solo en el teléfono. Sin internet no hace nada.
     */
    suspend fun sincronizarCatalogo() {
        val enLaNube = llamarListando { api.listarPrestadores() } ?: return
        enLaNube.forEach { guardarDeLaNube(it) }
        prestadorDao.listarSinRemoteId()
            .filter { it.perfilCompleto }
            .forEach { publicar(it) }
    }

    /**
     * Actualiza un prestador con lo último que hay en la API (GET /prestadores/{id}).
     * La pantalla se refresca sola porque observa Room.
     */
    suspend fun refrescar(prestadorId: Long) {
        val local = prestadorDao.buscarPorId(prestadorId) ?: return
        val idEnLaNube = local.remoteId ?: return
        val dto = llamarSinFallar { api.obtenerPrestador(idEnLaNube) } ?: return
        val enLaNube = dto.aModelo() ?: return
        prestadorDao.actualizar(enLaNube.copy(id = local.id, usuarioId = local.usuarioId))
    }

    /** Sube un prestador nuevo (POST) o actualiza el que ya está en la nube (PUT). */
    private suspend fun publicar(prestador: Prestador) {
        val idEnLaNube = prestador.remoteId
        if (idEnLaNube == null) {
            val creado = llamarSinFallar { api.crearPrestador(prestador.aDto()) }
            creado?.id?.let { nuevoId -> prestadorDao.asignarRemoteId(prestador.id, nuevoId) }
        } else {
            llamarSinFallar { api.actualizarPrestador(idEnLaNube, prestador.aDto()) }
        }
    }

    /**
     * Guarda en Room un prestador que vino de la API. Si ya existe aquí (mismo remoteId, o un prestador
     * de prueba con el mismo nombre que aún no estaba enlazado) lo actualiza; si no, lo inserta.
     */
    private suspend fun guardarDeLaNube(dto: PrestadorDto) {
        val enLaNube = dto.aModelo() ?: return
        val idEnLaNube = enLaNube.remoteId ?: return
        val local = prestadorDao.buscarPorRemoteId(idEnLaNube)
            ?: prestadorDao.buscarSinRemoteIdPorNombre(enLaNube.nombre)
        if (local == null) {
            prestadorDao.insertar(enLaNube)
        } else {
            prestadorDao.actualizar(enLaNube.copy(id = local.id, usuarioId = local.usuarioId))
        }
    }
}
