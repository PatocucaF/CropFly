package com.example.cropfly.ui.producer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequestScreen(
    onBackClick: () -> Unit,
    onRequestCreated: () -> Unit
) {
    var predio by remember { mutableStateOf("") }
    var tipoServicio by remember { mutableStateOf("") }
    var hectareas by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Solicitud de Vuelo") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = predio,
                onValueChange = { predio = it },
                label = { Text("Nombre del Predio / Fundo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tipoServicio,
                onValueChange = { tipoServicio = it },
                label = { Text("Tipo de Trabajo (ej. Pulverización)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = hectareas,
                onValueChange = { hectareas = it },
                label = { Text("Superficie (Hectáreas)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onRequestCreated,
                enabled = predio.isNotBlank() && tipoServicio.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Enviar Solicitud")
            }
        }
    }
}

