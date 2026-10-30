package com.example.cropfly.model

/**
 * Resultado de comparar una necesidad con un prestador.
 * afinidad va de 0 a 100. Los cuatro Boolean son las "píldoras" que se ven en cada tarjeta.
 */
data class ResultadoMatch(
    val prestador: Prestador,
    val afinidad: Int,
    val zona: Boolean,
    val cultivo: Boolean,
    val capacidad: Boolean,
    val disponible: Boolean
)

/**
 * Regla de negocio principal del MVP: el matching por puntaje.
 *
 * 1. Filtro obligatorio: el prestador debe estar activo y ofrecer el servicio pedido.
 * 2. Puntaje (suma 100): servicio 30 + zona 25 + cultivo 20 + capacidad 15 + disponibilidad 10.
 * 3. Orden: mayor afinidad primero; si empatan, mejor calificación primero.
 */
object Matching {
    const val PESO_SERVICIO = 30
    const val PESO_ZONA = 25
    const val PESO_CULTIVO = 20
    const val PESO_CAPACIDAD = 15
    const val PESO_DISPONIBILIDAD = 10

    fun buscar(necesidad: Necesidad, prestadores: List<Prestador>): List<ResultadoMatch> =
        prestadores
            .filter { it.activo && necesidad.servicio in it.servicios }
            .map { evaluar(necesidad, it) }
            .sortedWith(
                compareByDescending<ResultadoMatch> { it.afinidad }
                    .thenByDescending { it.prestador.calificacion }
            )

    fun evaluar(necesidad: Necesidad, prestador: Prestador): ResultadoMatch {
        val zona = necesidad.zona in prestador.cobertura
        val cultivo = necesidad.cultivo in prestador.cultivos
        // Capacidad: lo que el prestador alcanza a cubrir durante la ventana pedida.
        val diasDeTrabajo = Fechas.diasEnRango(necesidad.desde, necesidad.hasta)
        val capacidad = prestador.capacidadHaDia.toLong() * diasDeTrabajo >= necesidad.superficieHa
        // Disponibilidad: las dos ventanas de fechas se cruzan en algún día.
        val disponible = prestador.disponibleDesde <= necesidad.hasta &&
            prestador.disponibleHasta >= necesidad.desde

        var afinidad = PESO_SERVICIO
        if (zona) afinidad += PESO_ZONA
        if (cultivo) afinidad += PESO_CULTIVO
        if (capacidad) afinidad += PESO_CAPACIDAD
        if (disponible) afinidad += PESO_DISPONIBILIDAD

        return ResultadoMatch(prestador, afinidad, zona, cultivo, capacidad, disponible)
    }
}
