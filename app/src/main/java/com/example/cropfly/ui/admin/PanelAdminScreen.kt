package com.example.cropfly.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cropfly.model.Catalogo
import com.example.cropfly.model.Fechas
import com.example.cropfly.model.Necesidad
import com.example.cropfly.model.Prestador
import com.example.cropfly.model.Roles
import com.example.cropfly.model.Usuario
import com.example.cropfly.ui.components.BotonPrimario
import com.example.cropfly.ui.components.CampoSelector
import com.example.cropfly.ui.components.CampoTexto
import com.example.cropfly.ui.components.Estructura
import com.example.cropfly.ui.components.SelectorSegmentado
import com.example.cropfly.ui.components.TarjetaBlanca
import com.example.cropfly.ui.navigation.PestanaInferior
import com.example.cropfly.ui.theme.ChipBackground
import com.example.cropfly.ui.theme.DarkGreen
import com.example.cropfly.ui.theme.ErrorRed
import com.example.cropfly.ui.theme.FieldBorder
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextDark
import com.example.cropfly.ui.theme.TextMuted
import com.example.cropfly.viewmodel.AdminUiState
import com.example.cropfly.viewmodel.AdminViewModel
import com.example.cropfly.viewmodel.EliminacionPendiente
import com.example.cropfly.viewmodel.PestanaAdmin

/** Panel del administrador: usuarios, prestadores y necesidades de prueba. */
@Composable
fun PanelAdminScreen(
    usuarioId: Long,
    viewModel: AdminViewModel,
    alElegirPestana: (PestanaInferior) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        val mensaje = estado.mensaje
        if (mensaje != null) {
            viewModel.consumirMensaje()
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    Estructura(
        titulo = "Panel admin",
        usuarioId = usuarioId,
        pestanas = PestanaInferior.paraRol(Roles.ADMIN),
        pestanaSeleccionada = PestanaInferior.PANEL,
        alElegirPestana = alElegirPestana,
        snackbarHostState = snackbarHostState
    ) { margenes ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(margenes),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaDeCifra(estado.usuarios.size, "Usuarios", Modifier.weight(1f))
                    TarjetaDeCifra(estado.prestadoresActivos, "Prestadores activos", Modifier.weight(1f))
                    TarjetaDeCifra(estado.necesidades.size, "Necesidades", Modifier.weight(1f))
                }
            }
            item {
                SelectorSegmentado(
                    opciones = PestanaAdmin.entries.map { it.etiqueta },
                    seleccionada = estado.pestana.ordinal,
                    alElegir = { indice -> viewModel.cambiarPestana(PestanaAdmin.entries[indice]) }
                )
            }
            when (estado.pestana) {
                PestanaAdmin.USUARIOS -> items(estado.usuarios, key = { "u${it.id}" }) { usuario ->
                    FilaDeUsuario(usuario, alCambiar = { viewModel.cambiarUsuarioActivo(usuario, it) })
                }
                PestanaAdmin.PRESTADORES -> {
                    items(estado.prestadores, key = { "p${it.id}" }) { prestador ->
                        FilaDePrestador(
                            prestador = prestador,
                            alCambiar = { viewModel.cambiarPrestadorActivo(prestador, it) },
                            alEliminar = { viewModel.pedirEliminarPrestador(prestador) }
                        )
                    }
                    item { FormularioNuevoPrestador(estado, viewModel) }
                }
                PestanaAdmin.NECESIDADES -> {
                    if (estado.necesidades.isEmpty()) {
                        item {
                            TarjetaBlanca {
                                Text("No hay necesidades registradas", style = MaterialTheme.typography.titleMedium, color = TextDark)
                            }
                        }
                    }
                    items(estado.necesidades, key = { "n${it.id}" }) { necesidad ->
                        FilaDeNecesidad(necesidad, alEliminar = { viewModel.pedirEliminarNecesidad(necesidad) })
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, FieldBorder, RoundedCornerShape(16.dp))
                        .clickable(onClick = viewModel::reiniciarDatosDePrueba)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Restablecer datos de prueba", style = MaterialTheme.typography.titleMedium, color = TextDark)
                }
            }
        }
    }

    val pendiente = estado.eliminacionPendiente
    if (pendiente != null) {
        val nombre = when (pendiente) {
            is EliminacionPendiente.DePrestador -> pendiente.prestador.nombre
            is EliminacionPendiente.DeNecesidad -> "${pendiente.necesidad.servicio} (${pendiente.necesidad.zona})"
        }
        AlertDialog(
            onDismissRequest = viewModel::cancelarEliminacion,
            title = { Text("Eliminar elemento") },
            text = { Text("Se eliminará \"$nombre\" junto con los intereses asociados.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarEliminacion) { Text("Eliminar", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarEliminacion) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TarjetaDeCifra(cifra: Int, etiqueta: String, modifier: Modifier = Modifier) {
    TarjetaBlanca(modifier = modifier) {
        Text("$cifra", style = MaterialTheme.typography.headlineLarge, color = DarkGreen)
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = TextMuted, minLines = 2)
    }
}

@Composable
private fun Avatar(texto: String) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PrimaryGreen),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, color = Color.White, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun InterruptorAdmin(activo: Boolean, habilitado: Boolean, alCambiar: (Boolean) -> Unit) {
    Switch(
        checked = activo,
        onCheckedChange = alCambiar,
        enabled = habilitado,
        colors = SwitchDefaults.colors(
            checkedTrackColor = PrimaryGreen,
            checkedThumbColor = Color.White,
            uncheckedTrackColor = FieldBorder,
            uncheckedThumbColor = Color.White,
            uncheckedBorderColor = FieldBorder
        )
    )
}

@Composable
private fun BotonEliminar(alPulsar: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ChipBackground)
            .border(1.dp, FieldBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = alPulsar),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = TextDark)
    }
}

@Composable
private fun FilaDeUsuario(usuario: Usuario, alCambiar: (Boolean) -> Unit) {
    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(usuario.nombre.take(2).uppercase())
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.nombre, style = MaterialTheme.typography.titleMedium, color = TextDark)
                Text("${usuario.correo} · ${usuario.rol}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            InterruptorAdmin(usuario.activo, habilitado = usuario.rol != Roles.ADMIN, alCambiar = alCambiar)
        }
    }
}

@Composable
private fun FilaDePrestador(prestador: Prestador, alCambiar: (Boolean) -> Unit, alEliminar: () -> Unit) {
    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(prestador.nombre, style = MaterialTheme.typography.titleMedium, color = TextDark)
                val zonas = prestador.cobertura.joinToString(", ").ifEmpty { "Sin zonas" }
                Text(
                    "$zonas · ${prestador.capacidadHaDia} ha/día",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            InterruptorAdmin(prestador.activo, habilitado = true, alCambiar = alCambiar)
            Spacer(Modifier.width(8.dp))
            BotonEliminar(alEliminar)
        }
    }
}

@Composable
private fun FilaDeNecesidad(necesidad: Necesidad, alEliminar: () -> Unit) {
    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(necesidad.servicio, style = MaterialTheme.typography.titleMedium, color = TextDark)
                Text(
                    "${necesidad.cultivo} · ${necesidad.superficieHa} ha · ${necesidad.zona} · " +
                        Fechas.rango(necesidad.desde, necesidad.hasta),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Spacer(Modifier.width(8.dp))
            BotonEliminar(alEliminar)
        }
    }
}

/** Formulario "Agregar prestador ficticio" (con validación en el ViewModel). */
@Composable
private fun FormularioNuevoPrestador(estado: AdminUiState, viewModel: AdminViewModel) {
    val formulario = estado.formulario
    val errores = formulario.errores
    val mostrar = formulario.intentoEnvio

    TarjetaBlanca {
        Text("AGREGAR PRESTADOR FICTICIO", style = MaterialTheme.typography.titleLarge, color = DarkGreen)
        Spacer(Modifier.height(12.dp))
        CampoTexto(
            valor = formulario.nombre,
            alCambiar = viewModel::onNombreNuevoChange,
            etiqueta = "Nombre",
            placeholder = "AgroDron Sur",
            icono = Icons.Filled.Person,
            error = if (mostrar) errores.nombre else null
        )
        Spacer(Modifier.height(12.dp))
        CampoSelector(
            valor = formulario.zona,
            opciones = Catalogo.nombresDeZonas,
            alElegir = viewModel::onZonaNuevoChange,
            etiqueta = "Zona",
            icono = Icons.Filled.LocationOn,
            error = if (mostrar) errores.zona else null
        )
        Spacer(Modifier.height(12.dp))
        CampoSelector(
            valor = formulario.servicio,
            opciones = Catalogo.servicios,
            alElegir = viewModel::onServicioNuevoChange,
            etiqueta = "Servicio",
            icono = Icons.Filled.Build,
            error = if (mostrar) errores.servicio else null
        )
        Spacer(Modifier.height(12.dp))
        CampoTexto(
            valor = formulario.capacidad,
            alCambiar = viewModel::onCapacidadNuevoChange,
            etiqueta = "Capacidad",
            placeholder = "40",
            icono = Icons.Filled.Layers,
            sufijo = "ha/día",
            teclado = KeyboardType.Number,
            error = if (mostrar) errores.capacidad else null
        )
        Spacer(Modifier.height(16.dp))
        BotonPrimario("Agregar", viewModel::agregarPrestador, icono = Icons.Filled.Add)
    }
}
