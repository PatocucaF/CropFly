package com.example.cropfly.ui.producer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.cropfly.model.Fechas
import com.example.cropfly.model.ResultadoMatch
import com.example.cropfly.model.Roles
import com.example.cropfly.ui.components.BotonPrimario
import com.example.cropfly.ui.components.Estructura
import com.example.cropfly.ui.components.Pildora
import com.example.cropfly.ui.components.rememberAvisadorNativo
import com.example.cropfly.ui.navigation.PestanaInferior
import com.example.cropfly.ui.theme.AccentGreen
import com.example.cropfly.ui.theme.FieldBorder
import com.example.cropfly.ui.theme.LightGreenContainer
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextDark
import com.example.cropfly.ui.theme.TextMuted
import com.example.cropfly.ui.theme.WarningContainer
import com.example.cropfly.ui.theme.WarningText
import com.example.cropfly.viewmodel.ResultadosUiState
import com.example.cropfly.viewmodel.ResultadosViewModel

private val AnchoDeEtiqueta = 84.dp
private val ColorDeCeldaDestacada = Color(0xFFEAF4DC)

/**
 * Tabla que compara hasta 3 prestadores lado a lado y marca "MEJOR" en cada fila.
 * Comparte el mismo [ResultadosViewModel] que la pantalla de resultados (por eso recuerda la selección).
 */
@Composable
fun CompararScreen(
    usuarioId: Long,
    viewModel: ResultadosViewModel,
    alElegirPestana: (PestanaInferior) -> Unit,
    alVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val avisar = rememberAvisadorNativo()

    LaunchedEffect(estado.avisoInteres) {
        val prestador = estado.avisoInteres
        if (prestador != null) {
            avisar("Interés enviado", "Avisamos a $prestador (simulado).")
            viewModel.consumirAvisoInteres()
        }
    }

    Estructura(
        titulo = "Comparar",
        usuarioId = usuarioId,
        pestanas = PestanaInferior.paraRol(Roles.PRODUCTOR),
        pestanaSeleccionada = PestanaInferior.RESULTADOS,
        alElegirPestana = alElegirPestana,
        alVolver = alVolver
    ) { margenes ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(margenes)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            val necesidad = estado.necesidad
            if (necesidad == null || !estado.puedeComparar) {
                Text(
                    "Elige al menos ${ResultadosViewModel.MINIMO_PARA_COMPARAR} prestadores en Resultados para compararlos.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                BotonPrimario("Volver a resultados", alVolver)
            } else {
                Text(
                    text = buildAnnotatedString {
                        append("Comparación para ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextDark)) { append(necesidad.servicio) }
                        append(" en ${necesidad.zona}. Lo mejor de cada fila está marcado.")
                    },
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                TablaDeComparacion(estado, viewModel)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Manifestar interés es una simulación. La contratación y el pago quedan fuera del MVP.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    ModalConfirmarInteres(estado, viewModel)
}

@Composable
private fun TablaDeComparacion(estado: ResultadosUiState, viewModel: ResultadosViewModel) {
    val columnas = estado.comparados
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        FilaDeTabla(etiqueta = "") {
            columnas.forEach { Celda { Text(it.prestador.nombre.uppercase(), style = estiloDeEncabezado()) } }
        }
        FilaDeTabla("Afinidad") {
            columnas.forEach { columna ->
                val mejor = columna.afinidad == estado.mejorAfinidad
                Celda(destacada = mejor) {
                    Text("${columna.afinidad}%", style = MaterialTheme.typography.headlineMedium, color = TextDark)
                    if (mejor) EtiquetaMejor()
                }
            }
        }
        FilaDeTabla("Servicios") {
            columnas.forEach { Celda { TextoCelda(it.prestador.servicios.joinToString(", ")) } }
        }
        FilaDeTabla("Cobertura") {
            columnas.forEach { Celda { TextoCelda(it.prestador.cobertura.joinToString(", ")) } }
        }
        FilaDeTabla("Capacidad") {
            columnas.forEach { columna ->
                val mejor = columna.prestador.capacidadHaDia == estado.mejorCapacidad
                Celda(destacada = mejor) {
                    TextoCelda("${columna.prestador.capacidadHaDia} ha/día", resaltado = mejor)
                    if (mejor) EtiquetaMejor()
                }
            }
        }
        FilaDeTabla("Experiencia") {
            columnas.forEach { columna ->
                val mejor = columna.prestador.experienciaAnios == estado.mejorExperiencia
                Celda(destacada = mejor) {
                    TextoCelda("${columna.prestador.experienciaAnios} años", resaltado = mejor)
                    if (mejor) EtiquetaMejor()
                }
            }
        }
        FilaDeTabla("Disponible") {
            columnas.forEach { columna ->
                Celda {
                    TextoCelda(Fechas.rango(columna.prestador.disponibleDesde, columna.prestador.disponibleHasta))
                    Spacer(Modifier.height(6.dp))
                    PildoraDeVentana(columna)
                }
            }
        }
        FilaDeTabla("Calificación") {
            columnas.forEach { columna ->
                val mejor = columna.prestador.calificacion == estado.mejorCalificacion
                Celda(destacada = mejor) {
                    TextoCelda("${columna.prestador.calificacion} ★", resaltado = mejor)
                    if (mejor) EtiquetaMejor()
                }
            }
        }
        FilaDeTabla("Acción", ultima = true) {
            columnas.forEach { columna ->
                val enviado = columna.prestador.id in estado.interesesEnviados
                Celda {
                    Button(
                        onClick = { viewModel.pedirInteres(columna.prestador.id) },
                        enabled = !enviado,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            disabledContainerColor = PrimaryGreen.copy(alpha = 0.45f),
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text(if (enviado) "Enviado" else "Me interesa", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun estiloDeEncabezado(): TextStyle =
    MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, color = TextDark)

/** Una fila de la tabla: a la izquierda su nombre y a la derecha una celda por prestador. */
@Composable
private fun FilaDeTabla(
    etiqueta: String,
    ultima: Boolean = false,
    celdas: @Composable RowScope.() -> Unit
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Box(modifier = Modifier.width(AnchoDeEtiqueta).padding(horizontal = 8.dp, vertical = 12.dp)) {
            Text(etiqueta, color = TextMuted, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        celdas()
    }
    if (!ultima) HorizontalDivider(color = FieldBorder.copy(alpha = 0.6f))
}

@Composable
private fun RowScope.Celda(destacada: Boolean = false, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(if (destacada) ColorDeCeldaDestacada else Color.Transparent)
            .padding(horizontal = 6.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Top,
        content = contenido
    )
}

@Composable
private fun TextoCelda(texto: String, resaltado: Boolean = false) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall,
        color = TextDark,
        fontWeight = if (resaltado) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
private fun EtiquetaMejor() {
    Spacer(Modifier.height(4.dp))
    Text(
        text = "MEJOR",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.ExtraBold,
        color = TextDark,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AccentGreen)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun PildoraDeVentana(columna: ResultadoMatch) {
    if (columna.disponible) {
        Pildora("En tu ventana", fondo = LightGreenContainer, colorTexto = PrimaryGreen, icono = Icons.Filled.Check)
    } else {
        Pildora("Fuera de tu ventana", fondo = WarningContainer, colorTexto = WarningText, icono = Icons.Outlined.ErrorOutline)
    }
}
