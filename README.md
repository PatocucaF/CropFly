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
├── network/      Cliente Retrofit, interfaz de la API, DTOs y mapeadores (conexión con MockAPI)
├── repository/   Acceso a datos (único lugar que habla con los DAOs y con la API) y datos de prueba
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
- **Consumo de API REST:** Retrofit + Gson contra MockAPI.io, con 8 endpoints (GET, POST, PUT y DELETE). Ver la sección "API REST".
- **Persistencia con Room:** usuarios, prestadores, necesidades, intereses y notificaciones.
- **Sesión persistente:** al cerrar y volver a abrir la app se entra directo (bandera `sesionActiva` guardada en Room).
- **Navegación:** Navigation Compose con barra inferior distinta para cada perfil.
- **Animaciones:** pantalla de bienvenida, transición entre ingreso y registro, barra de afinidad, mensajes de error y estado de "buscando".
- **Modales:** permiso de ubicación, detalle de prestador, confirmación de interés, notificaciones y confirmaciones de borrado.
- **Recursos nativos:** cámara (foto del predio), notificaciones locales (Android 13+ pide permiso) y ubicación simulada.
- **Mapa:** al elegir la zona se muestra un mapa de OpenStreetMap con un marcador (necesita internet para cargar el mapa).
- **Imagen:** foto del predio, reducida y guardada solo en el teléfono.

## API REST (MockAPI.io)

La app consume una API REST propia creada en [MockAPI.io](https://mockapi.io). Tiene dos recursos: `prestadores` y `necesidades`.

**URL base:** `https://6b01ec384a8cad87bd8f6005.mockapi.io/` (se cambia en `network/ClienteApi.kt`).

| # | Método | Endpoint | Para qué se usa en la app |
|---|---|---|---|
| 1 | GET | `/prestadores` | Al abrir la app: descarga el catálogo de prestadores y lo guarda en Room. |
| 2 | GET | `/prestadores/{id}` | Al abrir el detalle de un prestador: confirma sus datos actuales. |
| 3 | POST | `/prestadores` | Publica un prestador nuevo (alta del administrador o perfil completo). |
| 4 | PUT | `/prestadores/{id}` | Actualiza el perfil o el estado activo/inactivo de un prestador. |
| 5 | DELETE | `/prestadores/{id}` | El administrador elimina un prestador del catálogo. |
| 6 | GET | `/necesidades?productorId=` | Inicio del productor: cuenta sus necesidades publicadas en la nube. |
| 7 | POST | `/necesidades` | Publica la necesidad que registra el productor. |
| 8 | DELETE | `/necesidades/{id}` | Elimina la necesidad cuando el productor la borra. |

### Cómo se conecta con el resto de la app
- **Room sigue siendo la fuente de la pantalla.** La API es una copia compartida en la nube: cada cambio se guarda primero en el teléfono y después se envía.
- **Funciona sin internet.** Si una llamada falla (sin conexión, error del servidor), el repositorio la ignora y la app sigue con los datos del teléfono. En Inicio se ve el aviso "Sin conexión: se usan los datos del teléfono".
- **DTO:** los datos viajan en objetos `*Dto` (`network/Dtos.kt`) y se convierten a las entidades de Room en `network/Mapeadores.kt`. Las listas (servicios, zonas, cultivos) viajan como un solo texto separado por `|`.
- **Identificador remoto:** cada prestador y necesidad guarda `remoteId`, el id que le puso MockAPI, para poder actualizarlo o borrarlo después.
- **Sincronización del catálogo:** `PrestadorRepository.sincronizarCatalogo()` baja los prestadores de la nube, actualiza los que ya existen, enlaza los de demostración por nombre y publica los locales que aún no están en la nube.
- Los "Me interesa" (`intereses`) se guardan solo en el teléfono, porque el plan gratuito de MockAPI permite 2 recursos.

### Limitaciones conocidas
- El plan gratuito de MockAPI limita la cantidad de recursos y de registros.
- El dueño de una necesidad se identifica con el id local del usuario, una simplificación propia de un MVP sin autenticación en la nube.

## Cómo iniciar sesión

La forma más rápida es usar los botones de la pantalla de ingreso:
1. Baja hasta **CUENTAS DEMO**.
2. Toca el perfil que quieras probar: **Productor**, **Prestador** o **Admin**.
3. El correo y la contraseña se rellenan solos.
4. Toca **Ingresar**.

Al entrar, la sesión queda guardada. Si cierras la app y la abres de nuevo, sigues dentro. Para cambiar de perfil usa **Cerrar sesión** en la pestaña **Cuenta**.

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
3. Ejecutar en un emulador o teléfono con Android 7.0 (API 24) o superior **con internet** (la API y el mapa lo necesitan).

La primera vez que se abre, la app sube a la nube los prestadores de demostración. Si cambia la versión de la base de datos, los datos locales se reinician y la demostración se vuelve a cargar.

## Pruebas unitarias

`./gradlew test` ejecuta 20 pruebas con JUnit:
- `MatchingTest` (7): regla de puntaje del matching.
- `ValidacionesTest` (8): validación de formularios.
- `MapeadoresTest` (5): conversión entre entidades y DTOs de la API.

## Generar el APK firmado

1. En Android Studio: **Build > Generate Signed App Bundle or APK… > APK**.
2. Crear o elegir un keystore **guardado fuera del repositorio** (los archivos `*.jks` y `*.keystore` están en `.gitignore`).
3. Elegir la variante **release** y las firmas **V1** y **V2**.
4. El APK queda en `app/release/app-release.apk`. Tiene el ícono personalizado del dron de CropFly.

## Equipo y gestión

- Integrantes: Catalina Vargas ([@Pancito-sty](https://github.com/Pancito-sty)) y Fernando Valenzuela ([@PatocucaF](https://github.com/PatocucaF)).
- Ramas: `main` (estable) y una rama de trabajo por integrante, con Pull Request hacia `main`.
- Tablero Trello: https://trello.com/invite/b/6ad644d188ff96e79efdbc5d/ATTI76b9a84464c03637fdcd3f229248979e98C52711/cropfly

