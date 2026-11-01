package com.example.vehicelboard.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vehicelboard.ui.theme.BoardColors
import com.example.vehicelboard.ui.viewmodel.DashBoardViewModel
import com.example.vehicelboard.usecase.SpeedStatus
import com.example.vehicelboard.usecase.VehicleUiState


@Composable
fun DashboardScreen(
    dashBoardViewModel: DashBoardViewModel = viewModel(factory = DashBoardViewModel.Factory)
) {
    val vehicleSignals by dashBoardViewModel.vehicleSignals.collectAsStateWithLifecycle()
    DashBoardContent(vehicleSignals)
}


@Composable
fun DashBoardContent(vehicleSignals: VehicleUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BoardColors.Background)
            .safeDrawingPadding()
            .padding(horizontal = 32.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val speedColor by animateColorAsState(
            targetValue = when {
                vehicleSignals.speedStatus != SpeedStatus.VALID -> BoardColors.Muted
                (vehicleSignals.speedKmh ?: 0) > 40 -> BoardColors.Amber
                else -> BoardColors.Blue
            },
            label = "arcColor",
        )

        Header(vehicleSignals)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = vehicleSignals.speedKmh?.toString() ?: "--",
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Bold,
                        color = speedColor,
                    )
                    Text(text = "km/h", fontSize = 22.sp, color = BoardColors.Muted)
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                MotorCard(
                    vehicleSignals.motorStatus, Modifier
                        .weight(0.8f)
                        .fillMaxWidth()
                )
                KillSwitchCard(
                    vehicleSignals.killSwitchStatus, Modifier
                        .weight(0.8f)
                        .fillMaxWidth()
                )
            }
        }
    }
}






