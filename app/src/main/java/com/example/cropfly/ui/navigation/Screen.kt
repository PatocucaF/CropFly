package com.example.cropfly.ui.navigation

sealed class Screen(val route: String) {
    object ProducerHome : Screen("producer_home")
    object NewRequest : Screen("new_request")
}