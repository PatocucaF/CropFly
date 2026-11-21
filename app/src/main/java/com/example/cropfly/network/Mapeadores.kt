package com.example.cropfly.network

import com.example.cropfly.model.Necesidad
import com.example.cropfly.model.Prestador

/** La API guarda las listas como un solo texto: "Maule|Ñuble|Biobío". */
private const val SEPARADOR_LISTAS = "|"

private fun List<String>.aTexto(): String = joinToString(SEPARADOR_LISTAS)

private fun String?.aLista(): List<String> =
    this?.split(SEPARADOR_LISTAS)?.filter { it.isNotBlank() } ?: emptyList()

/** Convierte un prestador de la base local en lo que se envía a la API. */
fun Prestador.aDto(): PrestadorDto = PrestadorDto(
    nombre = nombre,
    servicios = servicios.aTexto(),
    cobertura = cobertura.aTexto(),
    cultivos = cultivos.aTexto(),
    capacidadHaDia = capacidadHaDia,
    experienciaAnios = experienciaAnios,
    calificacion = calificacion,
    disponibleDesde = disponibleDesde,
    disponibleHasta = disponibleHasta,
    activo = activo
)

/** Convierte lo que responde la API en un prestador local. Devuelve null si faltan el id o el nombre. */
fun PrestadorDto.aModelo(): Prestador? {
    val idRemoto = id ?: return null
    val nombreRemoto = nombre ?: return null
    return Prestador(
        nombre = nombreRemoto,
        servicios = servicios.aLista(),
        cobertura = cobertura.aLista(),
        cultivos = cultivos.aLista(),
        capacidadHaDia = capacidadHaDia ?: 0,
        experienciaAnios = experienciaAnios ?: 0,
        calificacion = calificacion ?: 0.0,
        disponibleDesde = disponibleDesde ?: 0L,
        disponibleHasta = disponibleHasta ?: 0L,
        activo = activo ?: true,
        remoteId = idRemoto
    )
}

/** Convierte una necesidad de la base local en lo que se envía a la API. */
fun Necesidad.aDto(): NecesidadDto = NecesidadDto(
    productorId = productorId,
    servicio = servicio,
    cultivo = cultivo,
    superficieHa = superficieHa,
    zona = zona,
    desde = desde,
    hasta = hasta,
    producto = producto,
    notas = notas,
    creadaEn = creadaEn
)
