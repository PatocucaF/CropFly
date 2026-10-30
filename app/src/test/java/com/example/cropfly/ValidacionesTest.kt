package com.example.cropfly

import com.example.cropfly.viewmodel.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las reglas de validación de los formularios. */
class ValidacionesTest {

    @Test
    fun correoConFormatoValidoSeAcepta() {
        assertTrue(Validaciones.correoValido("productor@demo.cl"))
        assertFalse(Validaciones.correoValido("productor@demo"))
        assertFalse(Validaciones.correoValido("sin-arroba.cl"))
    }

    @Test
    fun claveDebeTenerOchoCaracteresLetrasYNumeros() {
        assertTrue(Validaciones.claveSegura("Demo1234"))
        assertFalse(Validaciones.claveSegura("Demo12"))
        assertFalse(Validaciones.claveSegura("sololetras"))
        assertFalse(Validaciones.claveSegura("12345678"))
    }

    @Test
    fun ingresoVacioMuestraLosDosErrores() {
        val errores = Validaciones.ingreso("", "")
        assertEquals("Ingresa tu correo", errores.correo)
        assertEquals("Ingresa tu contraseña", errores.clave)
    }

    @Test
    fun registroValidoNoTieneErrores() {
        val errores = Validaciones.registro("Productor Los Aromos", "a@demo.cl", "Demo1234", "Demo1234", true)
        assertFalse(errores.hayErrores)
    }

    @Test
    fun registroConClavesDistintasYSinTerminosMuestraErrores() {
        val errores = Validaciones.registro("Productor Los Aromos", "a@demo.cl", "Demo1234", "Demo9999", false)
        assertEquals("Las contraseñas no coinciden", errores.repetirClave)
        assertEquals("Debes aceptar para continuar", errores.terminos)
    }

    @Test
    fun necesidadVaciaMarcaTodosLosCamposObligatorios() {
        val errores = Validaciones.necesidad("", "", "", "", null, null, "")
        assertEquals("Elige el servicio que necesitas", errores.servicio)
        assertEquals("Elige el tipo de cultivo", errores.cultivo)
        assertEquals("Indica la superficie", errores.superficie)
        assertEquals("Elige la zona o usa el GPS simulado", errores.zona)
        assertEquals("Indica desde cuándo", errores.desde)
        assertEquals("Indica hasta cuándo", errores.hasta)
    }

    @Test
    fun fechaFinalAntesDeLaInicialEsError() {
        val errores = Validaciones.necesidad("Fumigación con dron", "Cerezo", "120", "Maule", 2_000L, 1_000L, "")
        assertEquals("No puede ser antes de la fecha inicial", errores.hasta)
    }

    @Test
    fun necesidadCompletaNoTieneErrores() {
        val errores = Validaciones.necesidad("Fumigación con dron", "Cerezo", "120", "Maule", 1_000L, 2_000L, "")
        assertFalse(errores.hayErrores)
        assertNull(errores.superficie)
    }
}
