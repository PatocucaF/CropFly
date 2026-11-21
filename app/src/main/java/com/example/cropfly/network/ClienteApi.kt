package com.example.cropfly.network

import com.google.gson.JsonParseException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/** Crea el cliente Retrofit una sola vez y lo comparte con los repositorios. */
object ClienteApi {
    /** Dirección base de la API en MockAPI.io. Debe terminar con "/". */
    private const val URL_BASE = "https://6b01ec384a8cad87bd8f6005.mockapi.io/"

    val api: CropFlyApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CropFlyApi::class.java)
    }
}

/**
 * Ejecuta una llamada a la API sin que la app se caiga si no hay internet o el servidor falla.
 * Devuelve null cuando algo sale mal; así la app sigue funcionando con los datos locales (Room).
 */
suspend fun <T> llamarSinFallar(llamada: suspend () -> T): T? = try {
    llamada()
} catch (error: IOException) {
    null
} catch (error: HttpException) {
    null
} catch (error: JsonParseException) {
    null
}

/**
 * Igual que [llamarSinFallar], pero para listas: MockAPI responde 404 cuando un filtro no encuentra nada,
 * y eso significa "lista vacía", no "error".
 */
suspend fun <T> llamarListando(llamada: suspend () -> List<T>): List<T>? = try {
    llamada()
} catch (error: HttpException) {
    if (error.code() == 404) emptyList() else null
} catch (error: IOException) {
    null
} catch (error: JsonParseException) {
    null
}
