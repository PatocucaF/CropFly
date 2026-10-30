package com.example.cropfly.ui.producer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.ViewColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.cropfly.model.Fechas
import com.example.cropfly.model.Necesidad
import com.example.cropfly.model.ResultadoMatch
import com.example.cropfly.model.Roles
import com.example.cropfly.ui.components.BotonPrimario
import com.example.cropfly.ui.components.BotonSecundario
import com.example.cropfly.ui.components.ChipSeleccion
import com.example.cropfly.ui.components.Estructura
import com.example.cropfly.ui.components.Pildora
import com.example.cropfly.ui.components.TarjetaBlanca
import com.example.cropfly.ui.components.rememberAvisadorNativo
import com.example.cropfly.ui.navigation.PestanaInferior
import com.example.cropfly.ui.theme.ChipBackground
import com.example.cropfly.ui.theme.DarkGreen
import com.example.cropfly.ui.theme.LightGreenContainer
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextDark
import com.example.cropfly.ui.theme.TextMuted
import com.example.cropfly.ui.theme.WarningContainer
import com.example.cropfly.ui.theme.WarningText
import com.example.cropfly.viewmodel.FiltroResultados
import com.example.cropfly.viewmodel.ResultadosViewModel

/** Prestadores compatibles con una necesidad, ordenados por afinidad. Permite filtrar, ver detalle y elegir para comparar. */
@Composable
fun ResultadosScreen(
    usuarioId: Long,
    viewModel: ResultadosViewModel,
    alElegirPestana: (PestanaInferior) -> Unit,
    alVolver: () -> Unit,
    alComparar: () -> Unit,
    alRegistrarNecesidad: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val avisar = rememberAvisadorNativo()

    // Recurso nativo: cuando se envía un interés, además del aviso en la campanita sale una notificación del sistema.
    LaunchedEffect(estado.avisoInteres) {
        val prestador = estado.avisoInteres
        if (prestador != null) {
            avisar("Interés enviado", "Avisamos a $prestador (simulado).")
            viewModel.consumirAvisoInteres()
        }
    }

    Estructura(
        titulo = "Resultados",
        usuarioId = usuarioId,
        pestanas = PestanaInferior.paraRol(Roles.PRODUCTOR),
        pestanaSeleccionada = PestanaInferior.RESULTADOS,
        alElegirPestana = alElegirPestana,
        alVolver = alVolver
    ) { margenes ->
        val necesidad = estado.necesidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(margenes)
        ) {
            if (!estado.cargando && necesidad == null) {
                SinNecesidades(alRegistrarNecesidad)
            } else if (necesidad != null) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item { ResumenDeNecesidad(necesidad) }
                    item {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FiltroResultados.entries.forEach { filtro ->
                                val texto = if (filtro == FiltroResultados.TODOS) {
                                    "${filtro.etiqueta} (${estado.resultados.size})"
                                } else {
                                    filtro.etiqueta
                                }
                                ChipSeleccion(
                                    texto = texto,
                                    seleccionado = estado.filtro == filtro,
                                    alPulsar = { viewModel.cambiarFiltro(filtro) }
                                )
                            }
                        }
                    }
                    if (estado.visibles.isEmpty()) {
                        item {
                            TarjetaBlanca {
                                Text(
                                    "No hay prestadores para este filtro.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextDark
                                )
                            }
                        }
                    }
                    items(estado.visibles, key = { it.prestador.id }) { resultado ->
                        val id = resultado.prestador.id
                        TarjetaDePrestador(
                            resultado = resultado,
                            seleccionado = id in estado.seleccionados,
                            seleccionLlena = estado.seleccionados.size >= ResultadosViewModel.MAXIMO_PARA_COMPARAR,
                            interesEnviado = id in estado.interesesEnviados,
                            alVerDetalle = { viewModel.abrirDetalle(id) },
                            alAlternarSeleccion = { viewModel.alternarSeleccion(id) }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = estado.seleccionados.isNotEmpty(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BarraDeSeleccion(
                    cantidad = estado.seleccionados.size,
                    puedeComparar = estado.puedeComparar,
                    alComparar = alComparar
                )
            }
        }
    }

    val detalle = estado.detalle
    if (detalle != null) {
        ModalDetallePrestador(detalle, detalle.prestador.id in estado.interesesEnviados, viewModel)
    }
    ModalConfirmarInteres(estado, viewModel)
}

@Composable
private fun SinNecesidades(alRegistrarNecesidad: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Aún no tienes resultados", style = MaterialTheme.typography.titleLarge, color = DarkGreen)
        Spacer(Modifier.height(8.dp))
        Text(
            "Registra una necesidad y te mostramos los prestadores compatibles.",
            color = TextMuted,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(20.dp))
        BotonPrimario("Registrar necesidad", alRegistrarNecesidad)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResumenDeNecesidad(necesidad: Necesidad) {
    TarjetaBlanca {
        Text(necesidad.servicio, style = MaterialTheme.typography.titleMedium, color = TextDark)
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val datos = listOf(
                necesidad.cultivo,
                "${necesidad.superficieHa} ha",
                necesidad.zona,
                Fechas.rango(necesidad.desde, necesidad.hasta)
            )
            datos.forEach { Pildora(it, fondo = ChipBackground, colorTexto = TextMuted) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaDePrestador(
    resultado: ResultadoMatch,
    seleccionado: Boolean,
    seleccionLlena: Boolean,
    interesEnviado: Boolean,
    alVerDetalle: () -> Unit,
    alAlternarSeleccion: () -> Unit
) {
    val prestador = resultado.prestador
    // La barra de afinidad parte vacía y se llena con animación al aparecer la tarjeta.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val progreso by animateFloatAsState(
        targetValue = if (visible) resultado.afinidad / 100f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "afinidad"
    )

    TarjetaBlanca {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(prestador.iniciales, color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(prestador.nombre, style = MaterialTheme.typography.titleMedium, color = TextDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${prestador.experienciaAnios} años · ", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Outlined.StarBorder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("${prestador.calificacion}", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${resultado.afinidad}", style = MaterialTheme.typography.headlineLarge, color = DarkGreen)
                Text("%", style = MaterialTheme.typography.titleSmall, color = TextMuted, modifier = Modifier.padding(bottom = 5.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = PrimaryGreen,
            trackColor = ChipBackground
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PildoraDeCriterio("Zona", resultado.zona)
            PildoraDeCriterio("Cultivo", resultado.cultivo)
            PildoraDeCriterio("Capacidad", resultado.capacidad)
            PildoraDeCriterio("Disponible", resultado.disponible)
            if (interesEnviado) {
                Pildora("Interés enviado", fondo = PrimaryGreen, colorTexto = Color.White, icono = Icons.Filled.Check)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BotonSecundario("Ver detalle", alVerDetalle, modifier = Modifier, alto = 46)
            val bloqueado = seleccionLlena && !seleccionado
            Button(
                onClick = alAlternarSeleccion,
                enabled = !bloqueado,
                modifier = Modifier.height(46.dp),
                shape = RoundedCornerShape(26.dp),
                colors = if (seleccionado) {
                    ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White)
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = ChipBackground,
                        contentColor = TextDark,
                        disabledContainerColor = ChipBackground,
                        disabledContentColor = TextMuted
                    )
                }
            ) {
                Icon(
                    imageVector = if (seleccionado) Icons.Filled.Check else Icons.Outlined.ViewColumn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (seleccionado) "Seleccionado" else "Comparar", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Píldora de un criterio del matching: verde con check si se cumple, ámbar con aviso si no. */
@Composable
private fun PildoraDeCriterio(texto: String, cumple: Boolean) {
    val icono: ImageVector = if (cumple) Icons.Filled.Check else Icons.Outlined.ErrorOutline
    Pildora(
        texto = texto,
        fondo = if (cumple) LightGreenContainer else WarningContainer,
        colorTexto = if (cumple) PrimaryGreen else WarningText,
        icono = icono
    )
}

@Composable
private fun BarraDeSeleccion(cantidad: Int, puedeComparar: Boolean, alComparar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (puedeComparar) {
                "$cantidad de ${ResultadosViewModel.MAXIMO_PARA_COMPARAR} seleccionados"
            } else {
                "Elige al menos ${ResultadosViewModel.MINIMO_PARA_COMPARAR} para comparar"
            },
            color = TextMuted,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        BotonPrimario(
            texto = "Comparar",
            alPulsar = alComparar,
            modifier = Modifier,
            habilitado = puedeComparar,
            alto = 48
        )
    }
}
