package com.example.vehicelboard.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vehicelboard.ui.theme.BoardColors
import com.example.vehicelboard.usecase.KillSwitchStatus
import com.example.vehicelboard.usecase.MotorStatus
import com.example.vehicelboard.usecase.RideMode
import com.example.vehicelboard.usecase.SpeedStatus
import com.example.vehicelboard.usecase.VehicleUiState

@Composable
fun MotorCard(status: MotorStatus, modifier: Modifier = Modifier) {
    val (value, badge, accent) = when (status) {
        MotorStatus.ON -> Triple("ON", "I", BoardColors.Green)
        MotorStatus.OFF -> Triple("OFF", "O", BoardColors.Muted)
        MotorStatus.UNKNOWN -> Triple("--", "?", BoardColors.Amber)
    }
    StatusCard(
        "MOTOR",
        value,
        badge,
        accent,
        filled = false,
        modifier = modifier
    )
}

@Composable
fun KillSwitchCard(status: KillSwitchStatus, modifier: Modifier = Modifier) {
    val card = when (status) {
        KillSwitchStatus.ACTIVE -> Triple("ACTIVE", "!", BoardColors.Red)
        KillSwitchStatus.INACTIVE -> Triple("INACTIVE", "\u2713", BoardColors.Green)
        KillSwitchStatus.UNKNOWN -> Triple("--", "?", BoardColors.Amber)
    }
    StatusCard(
        label = "KILL SWITCH",
        value = card.first,
        badge = card.second,
        accent = card.third,
        filled = status == KillSwitchStatus.ACTIVE,
        modifier = modifier,
    )
}

@Composable
private fun StatusCard(
    label: String,
    value: String,
    badge: String,
    accent: Color,
    filled: Boolean,
    modifier: Modifier = Modifier,
) {
    val container by animateColorAsState(
        if (filled) accent else BoardColors.Surface,
        label = "container"
    )
    val border by animateColorAsState(accent, label = "border")
    val content = if (filled) Color.White else BoardColors.OnSurface

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = container,
        border = BorderStroke(2.dp, border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(if (filled) Color.White else accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (filled) accent else BoardColors.Background,
                )
            }
            Column {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    color = content.copy(alpha = 0.75f)
                )
                Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = content)
            }
        }
    }
}

@Composable
fun Header(state: VehicleUiState) {
    val mode = state.rideMode
    val (label, color) = modeLabel(mode)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("RIDE", fontSize = 18.sp, letterSpacing = 4.sp, color = BoardColors.Muted)
            Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        }

        statusMessage(state)?.let {
            Text(
                it,
                fontSize = 14.sp,
                color = BoardColors.Amber,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun modeLabel(mode: RideMode): Pair<String, Color> = when (mode) {
    RideMode.PARKED -> "PARKED" to BoardColors.Muted
    RideMode.READY -> "READY" to BoardColors.Green
    RideMode.RIDING -> "RIDING" to BoardColors.Blue
    RideMode.ACCELERATING -> "ACCELERATING" to BoardColors.Blue
    RideMode.COASTING -> "COASTING" to BoardColors.Muted
    RideMode.MOTOR_DISABLED -> "MOTOR DISABLED" to BoardColors.Red
    RideMode.NO_SIGNAL -> "NO SIGNAL" to BoardColors.Amber
}

private fun statusMessage(state: VehicleUiState): String? = when {
    state.isStale -> "Vehicle waiting for signal"
    state.rideMode == RideMode.NO_SIGNAL -> "Connection Lost. Waiting for signal!"
    //state.speedStatus == SpeedStatus.INVALID -> "Invalid speed reading ignored"
    state.speedStatus == SpeedStatus.UNAVAILABLE -> "Connection Lost. Waiting for signal!"
    else -> null
}