package com.example.cropfly.model

/** Zona referencial y ficticia. Las coordenadas son aproximadas, solo para el mapa de demostración. */
data class Zona(val nombre: String, val latitud: Double, val longitud: Double)

/** Listas fijas que usan los formularios, los filtros y el matching. */
object Catalogo {
    val servicios = listOf(
        "Fumigación con dron",
        "Fertilización aérea",
        "Siembra con dron",
        "Fumigación con robot terrestre",
        "Monitoreo NDVI"
    )

    val cultivos = listOf(
        "Cerezo",
        "Uva de mesa",
        "Vid vinífera",
        "Arándano",
        "Manzano",
        "Palto",
        "Maíz",
        "Trigo",
        "Plantación forestal"
    )

    val productos = listOf(
        "Fungicida",
        "Herbicida",
        "Insecticida",
        "Fertilizante foliar",
        "Semilla certificada"
    )

    val zonas = listOf(
        Zona("Valparaíso", -33.0500, -71.6200),
        Zona("Metropolitana", -33.4500, -70.6600),
        Zona("O'Higgins", -34.1700, -70.7400),
        Zona("Maule", -35.4300, -71.6600),
        Zona("Ñuble", -36.6100, -72.1000),
        Zona("Biobío", -37.4700, -72.3500),
        Zona("Araucanía", -38.7400, -72.6000),
        Zona("Los Ríos", -39.8100, -73.2400)
    )

    val nombresDeZonas: List<String> = zonas.map { it.nombre }

    /** El botón "Usar GPS (simulado)" siempre devuelve esta zona. */
    const val ZONA_GPS_SIMULADO = "Maule"

    fun zonaPorNombre(nombre: String): Zona? = zonas.firstOrNull { it.nombre == nombre }
}
