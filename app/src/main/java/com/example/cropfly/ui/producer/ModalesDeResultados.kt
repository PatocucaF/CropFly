package com.example.cropfly.ui.producer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cropfly.model.Fechas
import com.example.cropfly.model.ResultadoMatch
import com.example.cropfly.ui.components.BotonPrimario
import com.example.cropfly.ui.components.BotonSecundario
import com.example.cropfly.ui.components.BotonesDeModal
import com.example.cropfly.ui.components.HojaModal
import com.example.cropfly.ui.components.TextoDeModal
import com.example.cropfly.ui.components.TituloDeModal
import com.example.cropfly.ui.theme.DarkGreen
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextDark
import com.example.cropfly.ui.theme.TextMuted
import com.example.cropfly.viewmodel.ResultadosUiState
import com.example.cropfly.viewmodel.ResultadosViewModel

/** Modal con la ficha completa de un prestador. */
@Composable
fun ModalDetallePrestador(
    resultado: ResultadoMatch,
    interesEnviado: Boolean,
    viewModel: ResultadosViewModel
) {
    val prestador = resultado.prestador
    HojaModal(alCerrar = viewModel::cerrarDetalle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(prestador.iniciales, color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    prestador.nombre.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = DarkGreen
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.StarBorder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${prestador.calificacion} calificación demostrativa",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        FilaDeDetalle("Servicios", prestador.servicios.joinToString(", "))
        FilaDeDetalle("Cobertura", prestador.cobertura.joinToString(", "))
        FilaDeDetalle("Cultivos", prestador.cultivos.joinToString(", "))
        FilaDeDetalle("Capacidad", "${prestador.capacidadHaDia} ha/día")
        FilaDeDetalle("Experiencia", "${prestador.experienciaAnios} años")
        FilaDeDetalle("Disponible", Fechas.rango(prestador.disponibleDesde, prestador.disponibleHasta))
        FilaDeDetalle("Compatibilidad", "${resultado.afinidad}%")
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            BotonSecundario("Cerrar", viewModel::cerrarDetalle, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            BotonPrimario(
                texto = if (interesEnviado) "Enviado" else "Me interesa",
                alPulsar = { viewModel.pedirInteres(prestador.id) },
                modifier = Modifier.weight(1f),
                habilitado = !interesEnviado
            )
        }
    }
}

@Composable
private fun FilaDeDetalle(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            etiqueta,
            color = TextMuted,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(120.dp)
        )
        Text(valor, color = TextDark, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

/** Modal "¿Enviar interés?": la vinculación es simulada, no hay contratación ni pago. */
@Composable
fun ModalConfirmarInteres(estado: ResultadosUiState, viewModel: ResultadosViewModel) {
    val pendiente = estado.interesPendiente ?: return
    HojaModal(alCerrar = viewModel::cancelarInteres) {
        TituloDeModal("¿Enviar interés?")
        TextoDeModal("Se avisará a ${pendiente.prestador.nombre} (simulado). No hay contratación ni pago en este MVP.")
        BotonesDeModal(
            textoSecundario = "Cancelar",
            alSecundario = viewModel::cancelarInteres,
            textoPrincipal = "Enviar interés",
            alPrincipal = viewModel::confirmarInteres
        )
    }
}
