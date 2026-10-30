package com.example.cropfly.ui.provider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cropfly.model.Catalogo
import com.example.cropfly.model.Roles
import com.example.cropfly.ui.components.BotonPrimario
import com.example.cropfly.ui.components.CampoFecha
import com.example.cropfly.ui.components.CampoTexto
import com.example.cropfly.ui.components.ChipSeleccion
import com.example.cropfly.ui.components.Estructura
import com.example.cropfly.ui.components.EtiquetaCampo
import com.example.cropfly.ui.components.TextoDeError
import com.example.cropfly.ui.navigation.PestanaInferior
import com.example.cropfly.ui.theme.TextMuted
import com.example.cropfly.viewmodel.PerfilPrestadorViewModel

/** El prestador edita aquí el perfil demostrativo que ven los productores al comparar. */
@Composable
fun PerfilPrestadorScreen(
    usuarioId: Long,
    viewModel: PerfilPrestadorViewModel,
    alElegirPestana: (PestanaInferior) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val errores = estado.errores
    val mostrar = estado.intentoEnvio
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            viewModel.consumirGuardado()
            snackbarHostState.showSnackbar("Perfil guardado")
        }
    }

    Estructura(
        titulo = "Mi perfil",
        usuarioId = usuarioId,
        pestanas = PestanaInferior.paraRol(Roles.PRESTADOR),
        pestanaSeleccionada = PestanaInferior.MI_PERFIL,
        alElegirPestana = alElegirPestana,
        snackbarHostState = snackbarHostState
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
                "Este perfil demostrativo es lo que ven los productores al comparar.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))
            CampoTexto(
                valor = estado.nombre,
                alCambiar = viewModel::onNombreChange,
                etiqueta = "Empresa (ficticia)",
                placeholder = "Drones Valle Sur",
                icono = Icons.Filled.Person,
                error = if (mostrar) errores.nombre else null
            )
            Spacer(Modifier.height(16.dp))
            GrupoDeChips(
                titulo = "Servicios que ofreces",
                opciones = Catalogo.servicios,
                seleccionadas = estado.servicios,
                alAlternar = viewModel::alternarServicio,
                error = if (mostrar) errores.servicios else null
            )
            Spacer(Modifier.height(16.dp))
            GrupoDeChips(
                titulo = "Cobertura geográfica",
                opciones = Catalogo.nombresDeZonas,
                seleccionadas = estado.cobertura,
                alAlternar = viewModel::alternarZona,
                error = if (mostrar) errores.cobertura else null
            )
            Spacer(Modifier.height(16.dp))
            GrupoDeChips(
                titulo = "Cultivos con experiencia",
                opciones = Catalogo.cultivos,
                seleccionadas = estado.cultivos,
                alAlternar = viewModel::alternarCultivo,
                error = if (mostrar) errores.cultivos else null
            )
            Spacer(Modifier.height(16.dp))
            Row {
                CampoTexto(
                    valor = estado.capacidad,
                    alCambiar = viewModel::onCapacidadChange,
                    etiqueta = "Capacidad",
                    placeholder = "60",
                    icono = Icons.Filled.Layers,
                    sufijo = "ha/día",
                    teclado = KeyboardType.Number,
                    error = if (mostrar) errores.capacidad else null,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                CampoTexto(
                    valor = estado.experiencia,
                    alCambiar = viewModel::onExperienciaChange,
                    etiqueta = "Experiencia",
                    placeholder = "6",
                    icono = Icons.Outlined.StarBorder,
                    sufijo = "años",
                    teclado = KeyboardType.Number,
                    error = if (mostrar) errores.experiencia else null,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))
            Row {
                CampoFecha(
                    valor = estado.desde,
                    alElegir = viewModel::onDesdeChange,
                    etiqueta = "Disponible desde",
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
            Spacer(Modifier.height(20.dp))
            BotonPrimario("Guardar perfil", viewModel::guardar)
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Título + píldoras que se marcan y desmarcan (selección múltiple), con mensaje de error si no se eligió ninguna. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrupoDeChips(
    titulo: String,
    opciones: List<String>,
    seleccionadas: Set<String>,
    alAlternar: (String) -> Unit,
    error: String?
) {
    Column {
        EtiquetaCampo(titulo)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            opciones.forEach { opcion ->
                ChipSeleccion(
                    texto = opcion,
                    seleccionado = opcion in seleccionadas,
                    alPulsar = { alAlternar(opcion) }
                )
            }
        }
        TextoDeError(error)
    }
}
