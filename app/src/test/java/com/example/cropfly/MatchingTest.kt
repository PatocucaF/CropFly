package com.example.cropfly

import com.example.cropfly.model.Fechas
import com.example.cropfly.model.Matching
import com.example.cropfly.model.Necesidad
import com.example.cropfly.model.Prestador
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de la regla de negocio principal: el matching por puntaje. */
class MatchingTest {

    private val necesidad = Necesidad(
        productorId = 1,
        servicio = "Fumigación con dron",
        cultivo = "Cerezo",
        superficieHa = 120,
        zona = "Maule",
        desde = Fechas.crear(2026, 10, 24),
        hasta = Fechas.crear(2026, 11, 3)
    )

    private val prestadorIdeal = Prestador(
        id = 1,
        nombre = "Drones Valle Sur",
        servicios = listOf("Fumigación con dron"),
        cobertura = listOf("Maule"),
        cultivos = listOf("Cerezo"),
        capacidadHaDia = 60,
        calificacion = 4.7,
        disponibleDesde = Fechas.crear(2026, 10, 21),
        disponibleHasta = Fechas.crear(2026, 12, 3)
    )

    @Test
    fun prestadorQueCumpleTodoObtiene100() {
        val resultado = Matching.evaluar(necesidad, prestadorIdeal)
        assertEquals(100, resultado.afinidad)
        assertTrue(resultado.zona && resultado.cultivo && resultado.capacidad && resultado.disponible)
    }

    @Test
    fun siNoCubreLaZonaPierde25Puntos() {
        val resultado = Matching.evaluar(necesidad, prestadorIdeal.copy(cobertura = listOf("Biobío")))
        assertEquals(75, resultado.afinidad)
        assertFalse(resultado.zona)
    }

    @Test
    fun siNoTieneElCultivoPierde20Puntos() {
        val resultado = Matching.evaluar(necesidad, prestadorIdeal.copy(cultivos = listOf("Trigo")))
        assertEquals(80, resultado.afinidad)
    }

    @Test
    fun siLaCapacidadNoAlcanzaPierde15Puntos() {
        // 5 ha/día durante 11 días son 55 ha, y la necesidad pide 120 ha.
        val resultado = Matching.evaluar(necesidad, prestadorIdeal.copy(capacidadHaDia = 5))
        assertEquals(85, resultado.afinidad)
        assertFalse(resultado.capacidad)
    }

    @Test
    fun siLasFechasNoSeCruzanPierde10Puntos() {
        val fueraDeVentana = prestadorIdeal.copy(
            disponibleDesde = Fechas.crear(2026, 11, 15),
            disponibleHasta = Fechas.crear(2026, 12, 20)
        )
        assertEquals(90, Matching.evaluar(necesidad, fueraDeVentana).afinidad)
    }

    @Test
    fun soloAparecenPrestadoresActivosQueOfrecenElServicio() {
        val otroServicio = prestadorIdeal.copy(id = 2, servicios = listOf("Monitoreo NDVI"))
        val inactivo = prestadorIdeal.copy(id = 3, activo = false)
        val resultados = Matching.buscar(necesidad, listOf(prestadorIdeal, otroServicio, inactivo))
        assertEquals(listOf(1L), resultados.map { it.prestador.id })
    }

    @Test
    fun siEmpatanGanaLaMejorCalificacion() {
        val mejorCalificado = prestadorIdeal.copy(id = 2, calificacion = 4.9)
        val resultados = Matching.buscar(necesidad, listOf(prestadorIdeal, mejorCalificado))
        assertEquals(listOf(2L, 1L), resultados.map { it.prestador.id })
    }
}
