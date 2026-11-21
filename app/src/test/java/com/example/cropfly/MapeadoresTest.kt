package com.example.cropfly

import com.example.cropfly.model.Necesidad
import com.example.cropfly.model.Prestador
import com.example.cropfly.network.PrestadorDto
import com.example.cropfly.network.aDto
import com.example.cropfly.network.aModelo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de la conversión entre las clases locales (Room) y los datos que viajan por la API. */
class MapeadoresTest {

    private val prestador = Prestador(
        nombre = "Drones Valle Sur",
        servicios = listOf("Fumigación con dron", "Fertilización aérea"),
        cobertura = listOf("Maule", "Ñuble"),
        cultivos = listOf("Cerezo"),
        capacidadHaDia = 60,
        experienciaAnios = 6,
        calificacion = 4.7,
        disponibleDesde = 1_000L,
        disponibleHasta = 2_000L
    )

    @Test
    fun prestadorEnviaLasListasComoTextoSeparadoPorBarra() {
        val dto = prestador.aDto()
        assertEquals("Fumigación con dron|Fertilización aérea", dto.servicios)
        assertEquals("Maule|Ñuble", dto.cobertura)
    }

    @Test
    fun prestadorRecibidoDeLaApiRecuperaSusListasYGuardaSuId() {
        val recibido = prestador.aDto().copy(id = "7").aModelo()
        assertEquals("7", recibido?.remoteId)
        assertEquals(prestador.servicios, recibido?.servicios)
        assertEquals(prestador.cobertura, recibido?.cobertura)
        assertEquals(60, recibido?.capacidadHaDia)
    }

    @Test
    fun prestadorSinIdONombreNoSePuedeGuardar() {
        assertNull(PrestadorDto(nombre = "Sin id").aModelo())
        assertNull(PrestadorDto(id = "3").aModelo())
    }

    @Test
    fun prestadorConCamposVaciosUsaValoresSeguros() {
        val recibido = PrestadorDto(id = "1", nombre = "Mínimo").aModelo()
        assertTrue(recibido?.servicios?.isEmpty() == true)
        assertEquals(true, recibido?.activo)
    }

    @Test
    fun necesidadEnviaSusDatosAlaApi() {
        val necesidad = Necesidad(
            productorId = 2L, servicio = "Cosecha", cultivo = "Cerezo", superficieHa = 120,
            zona = "Maule", desde = 10L, hasta = 20L
        )
        val dto = necesidad.aDto()
        assertEquals(2L, dto.productorId)
        assertEquals(120, dto.superficieHa)
        assertNull(dto.id)
    }
}
