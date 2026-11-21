package com.example.cropfly.network

/*
 * DTO = "Data Transfer Object": la forma exacta en que viaja cada dato por la API (en JSON).
 * Todos los campos son opcionales y tienen valor por defecto, porque Gson deja en null lo que la API no envíe.
 * Las listas viajan como un solo texto separado por "|" (ver Mapeadores.kt).
 */

/** Un prestador de servicios tal como lo guarda la API (recurso "prestadores"). */
data class PrestadorDto(
    val id: String? = null,
    val nombre: String? = null,
    val servicios: String? = null,
    val cobertura: String? = null,
    val cultivos: String? = null,
    val capacidadHaDia: Int? = null,
    val experienciaAnios: Int? = null,
    val calificacion: Double? = null,
    val disponibleDesde: Long? = null,
    val disponibleHasta: Long? = null,
    val activo: Boolean? = null
)

/** Una necesidad de servicio publicada por un productor (recurso "necesidades"). */
data class NecesidadDto(
    val id: String? = null,
    val productorId: Long? = null,
    val servicio: String? = null,
    val cultivo: String? = null,
    val superficieHa: Int? = null,
    val zona: String? = null,
    val desde: Long? = null,
    val hasta: Long? = null,
    val producto: String? = null,
    val notas: String? = null,
    val creadaEn: Long? = null
)
