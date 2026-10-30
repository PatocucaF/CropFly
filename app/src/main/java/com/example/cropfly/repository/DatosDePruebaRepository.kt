package com.example.cropfly.repository

import androidx.room.withTransaction
import com.example.cropfly.model.Catalogo
import com.example.cropfly.model.CropFlyDatabase
import com.example.cropfly.model.Fechas
import com.example.cropfly.model.Interes
import com.example.cropfly.model.Necesidad
import com.example.cropfly.model.Notificacion
import com.example.cropfly.model.Prestador
import com.example.cropfly.model.Roles
import com.example.cropfly.model.Usuario
import com.example.cropfly.util.Seguridad

/**
 * Datos ficticios del caso (el caso pide trabajar solo con datos sintéticos).
 * Crea las 3 cuentas demo, el catálogo de 8 prestadores y una necesidad de ejemplo.
 */
class DatosDePruebaRepository(private val baseDeDatos: CropFlyDatabase) {

    /** Se llama al abrir la app: solo siembra si la base está vacía (primera instalación). */
    suspend fun sembrarSiVacio() {
        if (baseDeDatos.usuarioDao().contar() == 0) {
            baseDeDatos.withTransaction {
                crearCuentasDemo()
                crearCatalogoYEjemplos()
            }
        }
    }

    /** Botón "Restablecer datos de prueba" del administrador. Mantiene las cuentas y la sesión. */
    suspend fun reiniciar() {
        baseDeDatos.withTransaction {
            baseDeDatos.interesDao().eliminarTodos()
            baseDeDatos.notificacionDao().eliminarTodas()
            baseDeDatos.necesidadDao().eliminarTodas()
            baseDeDatos.prestadorDao().eliminarTodos()
            baseDeDatos.usuarioDao().reactivarTodos()
            crearCatalogoYEjemplos()
            // Los prestadores que se registraron a mano recuperan su perfil vacío.
            baseDeDatos.usuarioDao().listarPorRol(Roles.PRESTADOR)
                .filter { it.correo != CORREO_PRESTADOR_DEMO }
                .forEach { baseDeDatos.prestadorDao().insertar(Prestador(usuarioId = it.id, nombre = it.nombre)) }
        }
    }

    private suspend fun crearCuentasDemo() {
        val usuarioDao = baseDeDatos.usuarioDao()
        val clave = Seguridad.hash(CLAVE_DEMO)
        usuarioDao.insertar(Usuario(nombre = "Productor Demo", correo = CORREO_PRODUCTOR_DEMO, claveHash = clave, rol = Roles.PRODUCTOR))
        usuarioDao.insertar(Usuario(nombre = "Prestador Demo", correo = CORREO_PRESTADOR_DEMO, claveHash = clave, rol = Roles.PRESTADOR))
        usuarioDao.insertar(Usuario(nombre = "Admin Demo", correo = CORREO_ADMIN_DEMO, claveHash = clave, rol = Roles.ADMIN))
    }

    private suspend fun crearCatalogoYEjemplos() {
        val usuarioDao = baseDeDatos.usuarioDao()
        val prestadorDao = baseDeDatos.prestadorDao()
        val productor = usuarioDao.buscarPorCorreo(CORREO_PRODUCTOR_DEMO) ?: return
        val usuarioPrestador = usuarioDao.buscarPorCorreo(CORREO_PRESTADOR_DEMO)

        // Drones Valle Sur es el perfil del "Prestador Demo": por eso lleva su usuarioId.
        val idDronesValleSur = prestadorDao.insertar(
            Prestador(
                usuarioId = usuarioPrestador?.id,
                nombre = "Drones Valle Sur",
                servicios = listOf("Fumigación con dron", "Fertilización aérea"),
                cobertura = listOf("Maule", "Ñuble", "Biobío"),
                cultivos = listOf("Cerezo", "Arándano", "Manzano", "Uva de mesa"),
                capacidadHaDia = 60,
                experienciaAnios = 6,
                calificacion = 4.7,
                disponibleDesde = Fechas.crear(2026, 10, 21),
                disponibleHasta = Fechas.crear(2026, 12, 3)
            )
        )
        prestadorDao.insertar(
            Prestador(
                nombre = "AeroAgro Maule",
                servicios = listOf("Fumigación con dron", "Siembra con dron"),
                cobertura = listOf("Maule", "O'Higgins"),
                cultivos = listOf("Cerezo", "Manzano", "Palto", "Uva de mesa"),
                capacidadHaDia = 45,
                experienciaAnios = 4,
                calificacion = 4.5,
                disponibleDesde = Fechas.crear(2026, 10, 29),
                disponibleHasta = Fechas.crear(2026, 12, 18)
            )
        )
        prestadorDao.insertar(
            Prestador(
                nombre = "Fumigaciones Cordillera",
                servicios = listOf("Fumigación con dron", "Fumigación con robot terrestre"),
                cobertura = listOf("O'Higgins", "Metropolitana"),
                cultivos = listOf("Cerezo", "Vid vinífera", "Uva de mesa", "Palto"),
                capacidadHaDia = 80,
                experienciaAnios = 11,
                calificacion = 4.2,
                disponibleDesde = Fechas.crear(2026, 11, 15),
                disponibleHasta = Fechas.crear(2026, 12, 20)
            )
        )
        prestadorDao.insertar(
            Prestador(
                nombre = "SkyCrop Ñuble",
                servicios = listOf("Fumigación con dron", "Monitoreo NDVI"),
                cobertura = listOf("Ñuble", "Biobío", "Maule"),
                cultivos = listOf("Arándano", "Trigo", "Maíz", "Plantación forestal"),
                capacidadHaDia = 55,
                experienciaAnios = 3,
                calificacion = 4.0,
                disponibleDesde = Fechas.crear(2026, 10, 20),
                disponibleHasta = Fechas.crear(2026, 11, 18)
            )
        )
        prestadorDao.insertar(
            Prestador(
                nombre = "Precisión Austral",
                servicios = listOf("Fumigación con robot terrestre", "Monitoreo NDVI"),
                cobertura = listOf("Araucanía", "Los Ríos", "Biobío"),
                cultivos = listOf("Trigo", "Maíz", "Plantación forestal"),
                capacidadHaDia = 90,
                experienciaAnios = 8,
                calificacion = 4.6,
                disponibleDesde = Fechas.crear(2026, 11, 1),
                disponibleHasta = Fechas.crear(2027, 1, 31)
            )
        )
        prestadorDao.insertar(
            Prestador(
                nombre = "AgroDron Aconcagua",
                servicios = listOf("Fumigación con dron", "Fertilización aérea"),
                cobertura = listOf("Valparaíso", "Metropolitana"),
                cultivos = listOf("Cerezo", "Palto", "Uva de mesa", "Vid vinífera"),
                capacidadHaDia = 40,
                experienciaAnios = 5,
                calificacion = 4.4,
                disponibleDesde = Fechas.crear(2026, 10, 25),
                disponibleHasta = Fechas.crear(2026, 11, 30)
            )
        )
        prestadorDao.insertar(
            Prestador(
                nombre = "Terra Aérea Biobío",
                servicios = listOf("Siembra con dron", "Fertilización aérea"),
                cobertura = listOf("Biobío", "Ñuble"),
                cultivos = listOf("Trigo", "Maíz", "Arándano"),
                capacidadHaDia = 30,
                experienciaAnios = 2,
                calificacion = 3.9,
                disponibleDesde = Fechas.crear(2026, 10, 15),
                disponibleHasta = Fechas.crear(2026, 12, 15)
            )
        )
        prestadorDao.insertar(
            Prestador(
                nombre = "Robótica del Sur",
                servicios = listOf("Fumigación con robot terrestre"),
                cobertura = listOf("Los Ríos", "Araucanía"),
                cultivos = listOf("Trigo", "Plantación forestal"),
                capacidadHaDia = 25,
                experienciaAnios = 3,
                calificacion = 4.1,
                disponibleDesde = Fechas.crear(2026, 11, 5),
                disponibleHasta = Fechas.crear(2027, 1, 15)
            )
        )

        // Una necesidad de ejemplo y su interés ya enviado, para que ninguna pantalla parta vacía.
        val maule = Catalogo.zonas.first { it.nombre == "Maule" }
        val idNecesidad = baseDeDatos.necesidadDao().insertar(
            Necesidad(
                productorId = productor.id,
                servicio = "Fumigación con dron",
                cultivo = "Cerezo",
                superficieHa = 120,
                zona = maule.nombre,
                latitud = maule.latitud,
                longitud = maule.longitud,
                desde = Fechas.crear(2026, 10, 24),
                hasta = Fechas.crear(2026, 11, 3),
                esEjemplo = true
            )
        )
        baseDeDatos.interesDao().insertar(
            Interes(necesidadId = idNecesidad, prestadorId = idDronesValleSur, productorId = productor.id)
        )
        if (usuarioPrestador != null) {
            baseDeDatos.notificacionDao().insertar(
                Notificacion(
                    usuarioId = usuarioPrestador.id,
                    mensaje = "Nueva solicitud: Fumigación con dron en Maule"
                )
            )
        }
    }

    companion object {
        const val CORREO_PRODUCTOR_DEMO = "productor@demo.cl"
        const val CORREO_PRESTADOR_DEMO = "prestador@demo.cl"
        const val CORREO_ADMIN_DEMO = "admin@demo.cl"
        const val CLAVE_DEMO = "Demo1234"
    }
}
