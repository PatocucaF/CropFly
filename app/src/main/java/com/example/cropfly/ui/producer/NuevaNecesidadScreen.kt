package com.example.cropfly.ui.producer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cropfly.model.Catalogo
import com.example.cropfly.model.Roles
import com.example.cropfly.ui.components.BotonPrimario
import com.example.cropfly.ui.components.BotonSecundario
import com.example.cropfly.ui.components.BotonesDeModal
import com.example.cropfly.ui.components.CampoFecha
import com.example.cropfly.ui.components.CampoSelector
import com.example.cropfly.ui.components.CampoTexto
import com.example.cropfly.ui.components.Estructura
import com.example.cropfly.ui.components.EtiquetaCampo
import com.example.cropfly.ui.components.HojaModal
import com.example.cropfly.ui.components.TextoDeError
import com.example.cropfly.ui.components.TextoDeModal
import com.example.cropfly.ui.components.TituloDeModal
import com.example.cropfly.ui.navigation.PestanaInferior
import com.example.cropfly.ui.theme.ChipBackground
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextMuted
import com.example.cropfly.util.Imagenes
import com.example.cropfly.viewmodel.NuevaNecesidadViewModel
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.cropfly.ui.components.MapaDeZona

/**
 * Formulario para registrar una necesidad de servicio.
 * Recursos nativos: cámara (foto del predio) y GPS simulado. Las validaciones las hace el ViewModel.
 */
@Composable
fun NuevaNecesidadScreen(
    usuarioId: Long,
    viewModel: NuevaNecesidadViewModel,
    alElegirPestana: (PestanaInferior) -> Unit,
    alVolver: () -> Unit,
    alCrearNecesidad: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val errores = estado.errores
    val mostrar = estado.intentoEnvio

    // Cuando el ViewModel termina de guardar, se navega a los resultados.
    LaunchedEffect(estado.necesidadCreadaId) {
        val id = estado.necesidadCreadaId
        if (id != null) {
            alCrearNecesidad(id)
            viewModel.consumirNavegacion()
        }
    }

    Estructura(
        titulo = "Nueva necesidad",
        usuarioId = usuarioId,
        pestanas = PestanaInferior.paraRol(Roles.PRODUCTOR),
        pestanaSeleccionada = PestanaInferior.NECESIDAD,
        alElegirPestana = alElegirPestana,
        alVolver = alVolver
    ) { margenes ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(margenes)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                "Describe el servicio que necesitas. Los datos son ficticios y no se envía nada real.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))

            CampoSelector(
                valor = estado.servicio,
                opciones = Catalogo.servicios,
                alElegir = viewModel::onServicioChange,
                etiqueta = "Servicio requerido",
                icono = Icons.Filled.Build,
                error = if (mostrar) errores.servicio else null,
                esValido = estado.servicio.isNotBlank()
            )
            Spacer(Modifier.height(14.dp))
            CampoSelector(
                valor = estado.cultivo,
                opciones = Catalogo.cultivos,
                alElegir = viewModel::onCultivoChange,
                etiqueta = "Tipo de cultivo",
                icono = Icons.Filled.Eco,
                error = if (mostrar) errores.cultivo else null,
                esValido = estado.cultivo.isNotBlank()
            )
            Spacer(Modifier.height(14.dp))
            CampoTexto(
                valor = estado.superficie,
                alCambiar = viewModel::onSuperficieChange,
                etiqueta = "Superficie",
                placeholder = "120",
                icono = Icons.Filled.Layers,
                sufijo = "ha",
                teclado = KeyboardType.Number,
                error = if (mostrar) errores.superficie else null,
                esValido = estado.superficie.isNotBlank() && errores.superficie == null
            )
            Spacer(Modifier.height(14.dp))
            CampoSelector(
                valor = estado.zona,
                opciones = Catalogo.nombresDeZonas,
                alElegir = viewModel::onZonaChange,
                etiqueta = "Zona referencial",
                icono = Icons.Filled.LocationOn,
                error = if (mostrar) errores.zona else null,
                esValido = estado.zona.isNotBlank()
            )
            Spacer(Modifier.height(12.dp))
            BotonSecundario(
                texto = "Usar GPS (simulado)",
                alPulsar = viewModel::usarGps,
                modifier = Modifier,
                icono = Icons.Filled.LocationOn,
                alto = 46
            )
            AvisoDeUbicacion(estado.usaGps, estado.zona, estado.avisoUbicacion)
            AnimatedVisibility(visible = Catalogo.zonaPorNombre(estado.zona) != null) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    MapaDeZona(
                        zona = Catalogo.zonaPorNombre(estado.zona),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row {
                CampoFecha(
                    valor = estado.desde,
                    alElegir = viewModel::onDesdeChange,
                    etiqueta = "Desde",
                    modifier = Modifier.weight(1f),
                    error = if (mostrar) errores.desde else null
                )
                Spacer(Modifier.width(12.dp))
                CampoFecha(
                    valor = estado.hasta,
                    alElegir = viewModel::onHastaChange,
                    etiqueta = "Hasta",
                    modifier = Modifier.weight(1f),
                    error = if (mostrar) errores.hasta else null
                )
            }
            Spacer(Modifier.height(14.dp))
            CampoSelector(
                valor = estado.producto,
                opciones = Catalogo.productos,
                alElegir = viewModel::onProductoChange,
                etiqueta = "Producto o aplicación",
                icono = Icons.Filled.Build,
                opcional = true,
                permitirVacio = true
            )
            Spacer(Modifier.height(14.dp))
            CampoTexto(
                valor = estado.notas,
                alCambiar = viewModel::onNotasChange,
                etiqueta = "Notas para el prestador",
                placeholder = "Ejemplo: acceso por camino lateral",
                opcional = true,
                lineas = 3,
                error = if (mostrar) errores.notas else null
            )
            Spacer(Modifier.height(14.dp))
            SelectorDeFoto(
                fotoPath = estado.fotoPath,
                errorFoto = estado.errorFoto,
                alElegirFoto = viewModel::onFotoElegida,
                alFallarFoto = viewModel::onFotoError,
                alQuitarFoto = viewModel::quitarFoto
            )
            Spacer(Modifier.height(20.dp))
            BotonPrimario(
                texto = "Buscar prestadores",
                alPulsar = viewModel::buscarPrestadores,
                icono = Icons.AutoMirrored.Filled.Send,
                cargando = estado.buscando
            )
            AnimatedVisibility(visible = estado.buscando) {
                Text(
                    "Buscando prestadores compatibles...",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (estado.mostrarPermisoUbicacion) {
        HojaModal(alCerrar = { viewModel.responderPermisoUbicacion(false) }) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChipBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = PrimaryGreen)
            }
            Spacer(Modifier.height(16.dp))
            TituloDeModal("¿Permitir ubicación?")
            TextoDeModal("CropFly usará una ubicación referencial ficticia para sugerir tu zona. No se guarda tu posición real.")
            BotonesDeModal(
                textoSecundario = "No permitir",
                alSecundario = { viewModel.responderPermisoUbicacion(false) },
                textoPrincipal = "Permitir",
                alPrincipal = { viewModel.responderPermisoUbicacion(true) }
            )
        }
    }
}

/** Texto bajo el botón de GPS: coordenadas ficticias si se usó, o aviso si no se dio permiso. */
@Composable
private fun AvisoDeUbicacion(usaGps: Boolean, zona: String, aviso: String?) {
    val zonaElegida = Catalogo.zonaPorNombre(zona)
    AnimatedVisibility(visible = usaGps && zonaElegida != null) {
        if (zonaElegida != null) {
            val latitud = String.format(Locale.US, "%.4f", zonaElegida.latitud)
            val longitud = String.format(Locale.US, "%.4f", zonaElegida.longitud)
            Text(
                "Ubicación simulada: $latitud, $longitud (zona ${zonaElegida.nombre}, ficticia)",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
    TextoDeError(aviso)
}

/**
 * Recurso nativo: cámara. Abre la app de cámara del teléfono (o la galería), reduce la foto y la guarda en la app.
 * Se avisa al ViewModel con la ruta del archivo; la pantalla solo muestra la miniatura.
 */
@Composable
private fun SelectorDeFoto(
    fotoPath: String?,
    errorFoto: String?,
    alElegirFoto: (String) -> Unit,
    alFallarFoto: () -> Unit,
    alQuitarFoto: () -> Unit
) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    var mostrarOpciones by remember { mutableStateOf(false) }
    var rutaTemporal by rememberSaveable { mutableStateOf<String?>(null) }

    fun procesar(origen: Uri) {
        alcance.launch {
            val ruta = withContext(Dispatchers.IO) { Imagenes.guardarReducida(contexto, origen) }
            if (ruta != null) alElegirFoto(ruta) else alFallarFoto()
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { sePudoTomar ->
        val temporal = rutaTemporal
        if (sePudoTomar && temporal != null) procesar(Uri.fromFile(File(temporal)))
        rutaTemporal = null
    }
    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) procesar(uri)
    }

    val miniatura by produceState<ImageBitmap?>(null, fotoPath) {
        value = if (fotoPath == null) {
            null
        } else {
            withContext(Dispatchers.IO) { Imagenes.cargarMiniatura(fotoPath)?.asImageBitmap() }
        }
    }

    EtiquetaCampo("Foto del predio", opcional = true)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ChipBackground),
            contentAlignment = Alignment.Center
        ) {
            val imagen = miniatura
            if (imagen != null) {
                Image(
                    bitmap = imagen,
                    contentDescription = "Foto del predio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = TextMuted)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column {
            BotonSecundario(
                texto = if (fotoPath == null) "Adjuntar imagen" else "Cambiar imagen",
                alPulsar = { mostrarOpciones = true },
                modifier = Modifier,
                icono = Icons.Outlined.Image,
                alto = 44
            )
            Spacer(Modifier.height(6.dp))
            if (fotoPath != null) {
                TextButton(onClick = alQuitarFoto) { Text("Quitar foto") }
            } else {
                Text("JPG o PNG, hasta 2 MB", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    TextoDeError(errorFoto)

    if (mostrarOpciones) {
        HojaModal(alCerrar = { mostrarOpciones = false }) {
            TituloDeModal("Foto del predio")
            TextoDeModal("Elige de dónde viene la imagen. Se guarda reducida, solo en este teléfono.")
            Spacer(Modifier.height(20.dp))
            BotonPrimario("Tomar foto", {
                mostrarOpciones = false
                val archivo = Imagenes.crearArchivoTemporal(contexto)
                rutaTemporal = archivo.absolutePath
                lanzadorCamara.launch(Imagenes.uriParaCamara(contexto, archivo))
            })
            Spacer(Modifier.height(10.dp))
            BotonSecundario("Elegir de la galería", {
                mostrarOpciones = false
                lanzadorGaleria.launch("image/*")
            })
        }
    }
}
