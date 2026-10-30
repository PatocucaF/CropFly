package com.example.cropfly.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cropfly.ui.theme.DarkGreen
import com.example.cropfly.ui.theme.TextMuted

/** Ventana modal que sube desde abajo (se usa para permisos, detalle, confirmaciones y notificaciones). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaModal(
    alCerrar: () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val estado = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = alCerrar,
        sheetState = estado,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            content = contenido
        )
    }
}

@Composable
fun TituloDeModal(texto: String) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.headlineMedium,
        color = DarkGreen
    )
}

@Composable
fun TextoDeModal(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyLarge,
        color = TextMuted,
        modifier = Modifier.padding(top = 8.dp)
    )
}

/** Fila de dos botones al pie de un modal: uno secundario (izquierda) y uno principal (derecha). */
@Composable
fun BotonesDeModal(
    textoSecundario: String,
    alSecundario: () -> Unit,
    textoPrincipal: String,
    alPrincipal: () -> Unit
) {
    Spacer(Modifier.height(24.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        BotonSecundario(textoSecundario, alSecundario, modifier = Modifier.weight(1f))
        Spacer(Modifier.padding(horizontal = 6.dp))
        BotonPrimario(textoPrincipal, alPrincipal, modifier = Modifier.weight(1f))
    }
}
