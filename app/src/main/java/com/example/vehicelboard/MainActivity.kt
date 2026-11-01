package com.example.vehicelboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.vehicelboard.ui.screens.DashboardScreen
import com.example.vehicelboard.ui.theme.VehicleBoardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VehicleBoardTheme {
                DashboardScreen()
            }
        }
    }
}