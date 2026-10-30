package com.example.cropfly.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cropfly.model.Fechas
import com.example.cropfly.ui.theme.ErrorContainer
import com.example.cropfly.ui.theme.ErrorRed
import com.example.cropfly.ui.theme.FieldBorder
import com.example.cropfly.ui.theme.PrimaryGreen
import com.example.cropfly.ui.theme.TextDark
import com.example.cropfly.ui.theme.TextMuted

private val FormaCampo = RoundedCornerShape(14.dp)

/** Título del campo que va arriba (ej: "Correo"), con "(opcional)" si corresponde. */
@Composable
fun EtiquetaCampo(texto: String, opcional: Boolean = false) {
    Row(modifier = Modifier.padding(bottom = 6.dp)) {
        Text(texto, style = MaterialTheme.typography.titleSmall, color = TextDark, fontWeight = FontWeight.Bold)
        if (opcional) {
            Text(" (opcional)", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }
    }
}

/** Mensaje rojo bajo el campo. Aparece y desaparece con animación. La pantalla solo lo muestra: el texto lo decide el ViewModel. */
@Composable
fun TextoDeError(mensaje: String?) {
    AnimatedVisibility(visible = mensaje != null) {
        Text(
            text = mensaje.orEmpty(),
            color = ErrorRed,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
        )
    }
}

/** Ícono al final del campo: signo de exclamación rojo si hay error, check verde si está correcto. */
@Composable
fun IconoDeEstado(hayError: Boolean, esValido: Boolean) {
    if (hayError) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = "Error", tint = ErrorRed)
    } else if (esValido) {
        Icon(Icons.Filled.Check, contentDescription = "Correcto", tint = PrimaryGreen)
    }
}

@Composable
private fun coloresDeCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    errorContainerColor = ErrorContainer,
    focusedBorderColor = PrimaryGreen,
    unfocusedBorderColor = FieldBorder,
    errorBorderColor = ErrorRed,
    focusedTextColor = TextDark,
    unfocusedTextColor = TextDark,
    errorTextColor = TextDark,
    cursorColor = PrimaryGreen
)

/**
 * Campo de texto con título, ícono, mensaje de error y check de "correcto".
 * [esClave] agrega el ojito para mostrar u ocultar la contraseña.
 */
@Composable
fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    icono: ImageVector? = null,
    error: String? = null,
    esValido: Boolean = false,
    opcional: Boolean = false,
    sufijo: String? = null,
    esClave: Boolean = false,
    teclado: KeyboardType = KeyboardType.Text,
    lineas: Int = 1
) {
    var claveVisible by remember { mutableStateOf(false) }
    val hayAdorno = esClave || sufijo != null || error != null || esValido

    Column(modifier = modifier) {
        EtiquetaCampo(etiqueta, opcional)
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextMuted) },
            leadingIcon = if (icono != null) {
                { Icon(icono, contentDescription = null, tint = TextMuted) }
            } else {
                null
            },
            trailingIcon = if (hayAdorno) {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (sufijo != null) {
                            Text(sufijo, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(8.dp))
                        }
                        if (esClave) {
                            IconButton(onClick = { claveVisible = !claveVisible }) {
                                Icon(
                                    imageVector = if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = TextMuted
                                )
                            }
                        }
                        IconoDeEstado(hayError = error != null, esValido = esValido)
                        Spacer(Modifier.width(12.dp))
                    }
                }
            } else {
                null
            },
            isError = error != null,
            singleLine = lineas == 1,
            minLines = lineas,
            visualTransformation = if (esClave && !claveVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (esClave) KeyboardType.Password else teclado),
            shape = FormaCampo,
            colors = coloresDeCampo()
        )
        TextoDeError(error)
    }
}

/**
 * Lista desplegable. Si [permitirVacio] es true aparece la opción "Sin selección" (para campos opcionales).
 * [esValido] cambia la flechita por un check verde cuando ya se eligió algo en un campo obligatorio.
 */
@Composable
fun CampoSelector(
    valor: String,
    opciones: List<String>,
    alElegir: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    esValido: Boolean = false,
    opcional: Boolean = false,
    permitirVacio: Boolean = false
) {
    var expandido by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        EtiquetaCampo(etiqueta, opcional)
        Box {
            OutlinedTextField(
                value = valor,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Selecciona...", color = TextDark) },
                leadingIcon = { Icon(icono, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (error != null || esValido) {
                            IconoDeEstado(hayError = error != null, esValido = esValido)
                        } else {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextMuted)
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                },
                isError = error != null,
                singleLine = true,
                shape = FormaCampo,
                colors = coloresDeCampo()
            )
            // Capa transparente que recibe el toque para abrir la lista (el campo es de solo lectura).
            Box(modifier = Modifier.matchParentSize().clickable { expandido = true })
            DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                if (permitirVacio) {
                    DropdownMenuItem(
                        text = { Text("Sin selección", color = TextMuted) },
                        onClick = {
                            alElegir("")
                            expandido = false
                        }
                    )
                }
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            alElegir(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
        TextoDeError(error)
    }
}

/** Campo de fecha: al tocarlo se abre el calendario de Material 3. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    valor: Long?,
    alElegir: (Long) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var mostrarCalendario by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        EtiquetaCampo(etiqueta)
        Box {
            OutlinedTextField(
                value = if (valor != null) Fechas.completa(valor) else "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("dd/mm/aaaa", color = TextMuted) },
                leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = TextMuted) },
                trailingIcon = if (error != null || valor != null) {
                    {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconoDeEstado(hayError = error != null, esValido = valor != null)
                            Spacer(Modifier.width(12.dp))
                        }
                    }
                } else {
                    null
                },
                isError = error != null,
                singleLine = true,
                shape = FormaCampo,
                colors = coloresDeCampo()
            )
            Box(modifier = Modifier.matchParentSize().clickable { mostrarCalendario = true })
        }
        TextoDeError(error)
    }

    if (mostrarCalendario) {
        val estadoCalendario = rememberDatePickerState(initialSelectedDateMillis = valor)
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estadoCalendario.selectedDateMillis?.let { alElegir(it) }
                        mostrarCalendario = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoCalendario)
        }
    }
}
