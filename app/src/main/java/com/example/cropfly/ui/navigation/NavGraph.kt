package com.example.cropfly.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// Rutas directas según tu estructura de carpetas
import com.example.cropfly.ui.navigation.Screen
import com.example.cropfly.ui.producer.ProducerHomeScreen
import com.example.cropfly.ui.producer.NewRequestScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.ProducerHome.route
    ) {
        composable(Screen.ProducerHome.route) {
            ProducerHomeScreen(
                onNewRequestClick = {
                    navController.navigate(Screen.NewRequest.route)
                }
            )
        }

        composable(Screen.NewRequest.route) {
            NewRequestScreen(
                onBackClick = { navController.popBackStack() },
                onRequestCreated = {
                    Toast.makeText(context, "¡Solicitud creada exitosamente!", Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }
            )
        }
    }
}