# CropFly

MVP móvil Android que vincula **productores agrícolas** con **prestadores de servicios** (fumigación y siembra con dron, robots terrestres, monitoreo NDVI).
Proyecto de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC), basado en el caso **TECMUNDO**.

> Todo el contenido es **ficticio**: no hay datos personales, productivos ni comerciales reales.

## Qué hace la app

| Perfil | Qué puede hacer |
|---|---|
| **Productor** | Registrar una necesidad de servicio, ver prestadores compatibles ordenados por afinidad, compararlos y manifestar interés (simulado). |
| **Prestador** | Mantener su perfil demostrativo (servicios, cobertura, cultivos, capacidad, experiencia, disponibilidad) y revisar las solicitudes recibidas. |
| **Administrador** | Activar o desactivar usuarios y prestadores, agregar prestadores ficticios, revisar necesidades y restablecer los datos de prueba. |

### Regla de negocio principal: matching por puntaje
El prestador debe estar activo y ofrecer el servicio pedido. Luego suma puntos (total 100):

| Criterio | Puntos |
|---|---|
| Servicio ofrecido | 30 |
| Cubre la zona | 25 |
| Tiene experiencia en el cultivo | 20 |
| Capacidad suficiente (ha/día × días de la ventana ≥ superficie) | 15 |
| Disponibilidad que se cruza con la ventana pedida | 10 |

Se ordena por afinidad y, si empatan, por calificación (demostrativa).

## Arquitectura (MVVM)

```
com.example.cropfly
├── model/        Entidades Room, DAOs, base de datos, catálogo, fechas y regla de matching
├── repository/   Acceso a datos (único lugar que habla con los DAOs) y datos de prueba
├── viewmodel/    Estado de cada pantalla, validaciones y fábrica de ViewModels
├── ui/
│   ├── auth/         Splash, ingreso y registro
│   ├── producer/     Inicio, nueva necesidad, resultados y comparación
│   ├── provider/     Solicitudes y perfil del prestador
│   ├── admin/        Panel del administrador
│   ├── account/      Mi cuenta (los tres perfiles)
│   ├── components/   Campos, botones, modales y marco de pantalla reutilizables
│   ├── navigation/   Rutas, barra inferior y NavGraph
│   └── theme/        Colores, tipografía y tema
└── util/         Seguridad (hash), imágenes y notificaciones
```

Las pantallas (`ui`) solo muestran el estado que entrega el ViewModel y le avisan lo que hace el usuario. Las **validaciones** viven en `viewmodel/Validaciones.kt`.

## Requisitos del curso

- **Formularios y validación:** ingreso, registro, nueva necesidad, perfil del prestador y alta de prestador (mensajes bajo cada campo, íconos de error y de correcto).
- **Persistencia con Room:** usuarios, prestadores, necesidades, intereses y notificaciones.
- **Sesión persistente:** al cerrar y volver a abrir la app se entra directo (bandera `sesionActiva` guardada en Room).
- **Navegación:** Navigation Compose con barra inferior distinta para cada perfil.
- **Animaciones:** pantalla de bienvenida, transición entre ingreso y registro, barra de afinidad, mensajes de error y estado de "buscando".
- **Modales:** permiso de ubicación, detalle de prestador, confirmación de interés, notificaciones y confirmaciones de borrado.
- **Recursos nativos:** cámara (foto del predio), notificaciones locales (Android 13+ pide permiso) y ubicación simulada.
- **Imagen:** foto del predio, reducida y guardada solo en el teléfono.

## Cuentas de prueba

| Perfil | Correo | Contraseña |
|---|---|---|
| Productor | `productor@demo.cl` | `Demo1234` |
| Prestador | `prestador@demo.cl` | `Demo1234` |
| Administrador | `admin@demo.cl` | `Demo1234` |

La pantalla de ingreso tiene botones "Cuentas demo" que rellenan el formulario. En el panel del administrador, "Restablecer datos de prueba" deja todo como al instalar.

## Cómo ejecutarlo

1. Abrir el proyecto en Android Studio (AGP 8.7, Kotlin 2.0, JDK 11 o superior).
2. Esperar la sincronización de Gradle.
3. Ejecutar en un emulador o teléfono con Android 7.0 (API 24) o superior.

Pruebas unitarias (matching y validaciones): `./gradlew test`.

## Equipo y gestión

- Integrantes: Catalina Vargas ([@Pancito-sty](https://github.com/Pancito-sty)) y Fernando Valenzuela ([@PatocucaF](https://github.com/PatocucaF)).
- Ramas: `main` (estable) y una rama de trabajo por integrante, con Pull Request hacia `main`.
- Tablero Trello: https://trello.com/invite/b/6ad644d188ff96e79efdbc5d/ATTI76b9a84464c03637fdcd3f229248979e98C52711/cropfly
- Video de demostración: _(agregar enlace)_.

