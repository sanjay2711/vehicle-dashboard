package com.example.vehicelboard.usecase

import com.example.vehicelboard.data.VehicleRepoImpl
import com.example.vehicelboard.data.VehicleRepository
import com.example.vehicelboard.data.VehicleSignal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlin.math.roundToInt

class VehicleUseCase(
    private val vehicleRepository: VehicleRepository = VehicleRepoImpl()
) {

    operator fun invoke(): Flow<VehicleUiState> {
        return vehicleRepository.signals.map { it.toUiState() }
        }

}

internal fun VehicleSignal.toUiState(): VehicleUiState {
    val vehicleSpeed = speedKmh
    val (speed, speedStatus) = when {
        vehicleSpeed == null -> null to SpeedStatus.UNAVAILABLE
        !vehicleSpeed.isFinite() || vehicleSpeed < 0f -> null to SpeedStatus.INVALID
        else -> vehicleSpeed.roundToInt() to SpeedStatus.VALID
    }


    return VehicleUiState(
        speedKmh = speed,
        speedStatus = speedStatus,
        motorStatus = when (motorOn) {
            true -> MotorStatus.ON
            false -> MotorStatus.OFF
            null -> MotorStatus.UNKNOWN
        },
        killSwitchStatus = when (killSwitchActive) {
            true -> KillSwitchStatus.ACTIVE
            false -> KillSwitchStatus.INACTIVE
            null -> KillSwitchStatus.UNKNOWN
        }
    )
}


