package com.example.vehicelboard.usecase


enum class SpeedStatus { VALID, INVALID, UNAVAILABLE }
enum class MotorStatus { ON, OFF, UNKNOWN }
enum class KillSwitchStatus { ACTIVE, INACTIVE, UNKNOWN }
enum class RideMode { PARKED, READY, RIDING, ACCELERATING, COASTING, MOTOR_DISABLED, NO_SIGNAL }

data class VehicleUiState(
    val speedKmh: Int? = null,
    val speedStatus: SpeedStatus = SpeedStatus.UNAVAILABLE,
    val motorStatus: MotorStatus = MotorStatus.UNKNOWN,
    val killSwitchStatus: KillSwitchStatus = KillSwitchStatus.UNKNOWN,
    val isStale: Boolean = false
) {


    private val noData: Boolean = speedKmh == null && speedStatus == SpeedStatus.UNAVAILABLE

    val rideMode: RideMode
        get() = when {
            isStale || noData -> RideMode.NO_SIGNAL
            killSwitchStatus == KillSwitchStatus.ACTIVE -> RideMode.MOTOR_DISABLED
            speedStatus != SpeedStatus.VALID || motorStatus == MotorStatus.UNKNOWN || killSwitchStatus == KillSwitchStatus.UNKNOWN -> RideMode.NO_SIGNAL
            else -> {
                val speed = speedKmh ?: 0
                when (motorStatus) {
                    MotorStatus.OFF -> if (speed > 0) RideMode.COASTING else RideMode.PARKED
                    else -> when {
                        speed == 0 -> RideMode.READY
                        speed <= ACCELERATING_ABOVE_KMH -> RideMode.RIDING
                        else -> RideMode.ACCELERATING
                    }
                }
            }
        }

    fun timeoutState(): VehicleUiState = copy(
        speedKmh = null,
        speedStatus = SpeedStatus.UNAVAILABLE,
        motorStatus = MotorStatus.UNKNOWN,
        killSwitchStatus = KillSwitchStatus.UNKNOWN,
        isStale = true,
    )


    companion object {
        const val ACCELERATING_ABOVE_KMH = 30
        val Initial = VehicleUiState(
            speedKmh = null,
            speedStatus = SpeedStatus.UNAVAILABLE,
            motorStatus = MotorStatus.UNKNOWN,
            killSwitchStatus = KillSwitchStatus.UNKNOWN,
            isStale = false
        )
    }
}