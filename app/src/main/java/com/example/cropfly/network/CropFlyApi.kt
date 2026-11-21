package com.example.cropfly.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Los 8 endpoints de la API REST de CropFly (alojada en MockAPI.io).
 * Retrofit genera el código que hace cada llamada HTTP; cada función "suspend" corre sin bloquear la pantalla.
 */
interface CropFlyApi {

    // ---- Prestadores ----

    /** GET /prestadores: el catálogo completo de prestadores. */
    @GET("prestadores")
    suspend fun listarPrestadores(): List<PrestadorDto>

    /** GET /prestadores/{id}: un prestador (sirve para confirmar que sigue activo). */
    @GET("prestadores/{id}")
    suspend fun obtenerPrestador(@Path("id") id: String): PrestadorDto

    /** POST /prestadores: publica un prestador nuevo. */
    @POST("prestadores")
    suspend fun crearPrestador(@Body prestador: PrestadorDto): PrestadorDto

    /** PUT /prestadores/{id}: actualiza el perfil de un prestador. */
    @PUT("prestadores/{id}")
    suspend fun actualizarPrestador(@Path("id") id: String, @Body prestador: PrestadorDto): PrestadorDto

    /** DELETE /prestadores/{id}: elimina un prestador del catálogo. */
    @DELETE("prestadores/{id}")
    suspend fun eliminarPrestador(@Path("id") id: String): PrestadorDto

    // ---- Necesidades ----

    /** GET /necesidades?productorId=: las necesidades publicadas por un productor. */
    @GET("necesidades")
    suspend fun listarNecesidades(@Query("productorId") productorId: Long): List<NecesidadDto>

    /** POST /necesidades: publica una necesidad nueva. */
    @POST("necesidades")
    suspend fun crearNecesidad(@Body necesidad: NecesidadDto): NecesidadDto

    /** DELETE /necesidades/{id}: elimina una necesidad. */
    @DELETE("necesidades/{id}")
    suspend fun eliminarNecesidad(@Path("id") id: String): NecesidadDto
}
