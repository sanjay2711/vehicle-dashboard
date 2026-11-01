package com.example.vehicelboard

import com.example.vehicelboard.data.VehicleRepoImpl
import com.example.vehicelboard.data.VehicleSignal
import com.example.vehicelboard.usecase.KillSwitchStatus
import com.example.vehicelboard.usecase.MotorStatus
import com.example.vehicelboard.usecase.RideMode
import com.example.vehicelboard.usecase.SpeedStatus
import com.example.vehicelboard.usecase.VehicleUiState
import com.example.vehicelboard.usecase.toUiState
import junit.framework.TestCase.assertEquals
import org.junit.Test

class VehicleUiStateMapperTest {

    @Test
    fun `five required scenarios map to the expected ride modes`() {
        val expected = listOf(
            VehicleRepoImpl.PARKED to RideMode.PARKED,
            VehicleRepoImpl.READY to RideMode.READY,
            VehicleRepoImpl.RIDING to RideMode.RIDING,
            VehicleRepoImpl.ACCELERATING to RideMode.ACCELERATING,
            VehicleRepoImpl.MOTOR_DISABLED to RideMode.MOTOR_DISABLED,
        )
        expected.forEach { (signal, mode) -> assertEquals(mode, signal.toUiState().rideMode) }
    }


    @Test
    fun `missing signals become unknown instead of defaulting to a value`() {
        val state = VehicleSignal(null, null, null).toUiState()
        assertEquals(SpeedStatus.UNAVAILABLE, state.speedStatus)
        assertEquals(MotorStatus.UNKNOWN, state.motorStatus)
        assertEquals(KillSwitchStatus.UNKNOWN, state.killSwitchStatus)
        assertEquals(RideMode.NO_SIGNAL, state.rideMode)
    }

    @Test
    fun `asStale hides every live value`() {
        val stale = VehicleSignal(50f, true, false).toUiState().timeoutState()
        assertEquals(VehicleUiState.Initial.copy(isStale = true), stale)
    }
}