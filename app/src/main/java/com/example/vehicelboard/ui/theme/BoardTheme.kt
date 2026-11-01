package com.example.vehicelboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


object BoardColors {
    val Background = Color(0xFF0B0F14)
    val Surface = Color(0xFF151B23)
    val OnSurface = Color(0xFFE6EDF3)
    val Muted = Color(0xFF7D8590)
    val Green = Color(0xFF3FB950)
    val Red = Color(0xFFE5332A)
    val Amber = Color(0xFFFF5722)
    val Blue = Color(0xFF58A6FF)
}

@Composable
fun VehicleBoardTheme(
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = BoardColors.Background,
            surface = BoardColors.Surface,
            onSurface = BoardColors.OnSurface,
        ),
        content = content
    )
}