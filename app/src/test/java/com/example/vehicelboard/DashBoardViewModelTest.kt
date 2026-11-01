package com.example.vehicelboard

import com.example.vehicelboard.data.VehicleRepoImpl
import com.example.vehicelboard.data.VehicleRepository
import com.example.vehicelboard.data.VehicleSignal
import com.example.vehicelboard.ui.viewmodel.DashBoardViewModel
import com.example.vehicelboard.usecase.KillSwitchStatus
import com.example.vehicelboard.usecase.MotorStatus
import com.example.vehicelboard.usecase.RideMode
import com.example.vehicelboard.usecase.SpeedStatus
import com.example.vehicelboard.usecase.VehicleUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DashBoardViewModelTest {

    private class FakeSource : VehicleRepository {
        val flow = MutableSharedFlow<VehicleSignal>()
        override val signals: Flow<VehicleSignal> = flow
    }

    private lateinit var vm : DashBoardViewModel
    private lateinit var source : VehicleUseCase
    private lateinit var fakeRepo : FakeSource

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeRepo  = FakeSource()
        source = VehicleUseCase(fakeRepo)
        vm = DashBoardViewModel(source)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `ui state follows emitted signals through all five scenarios`() = runTest {
        backgroundScope.launch { vm.vehicleSignals.collect {} }
        runCurrent()
        val steps = listOf(
            VehicleRepoImpl.PARKED to Triple(0, MotorStatus.OFF, KillSwitchStatus.INACTIVE),
            VehicleRepoImpl.READY to Triple(0, MotorStatus.ON, KillSwitchStatus.INACTIVE),
            VehicleRepoImpl.RIDING to Triple(25, MotorStatus.ON, KillSwitchStatus.INACTIVE),
            VehicleRepoImpl.ACCELERATING to Triple(50, MotorStatus.ON, KillSwitchStatus.INACTIVE),
            VehicleRepoImpl.MOTOR_DISABLED to Triple(0, MotorStatus.OFF, KillSwitchStatus.ACTIVE),
        )
        for ((signal, expected) in steps) {
            fakeRepo.flow.emit(signal)
            runCurrent()
            val state = vm.vehicleSignals.value
            assertEquals(expected.first, state.speedKmh)
            assertEquals(expected.second, state.motorStatus)
            assertEquals(expected.third, state.killSwitchStatus)
            assertFalse(state.isStale)
        }
    }

    @Test
    fun `invalid speed does not crash and is flagged`() = runTest {
        backgroundScope.launch { vm.vehicleSignals.collect {} }
        runCurrent()
        fakeRepo.flow.emit(VehicleRepoImpl.INVALID_SPEED)
        runCurrent()

        assertEquals(SpeedStatus.INVALID, vm.vehicleSignals.value.speedStatus)
    }

    @Test
    fun `data goes stale after timeout and recovers on the next signal`() = runTest {
        backgroundScope.launch { vm.vehicleSignals.collect {} }
        runCurrent()

        fakeRepo.flow.emit(VehicleRepoImpl.ACCELERATING)
        runCurrent()
        advanceTimeBy(2_900); runCurrent()
        assertFalse(vm.vehicleSignals.value.isStale)

        advanceTimeBy(200); runCurrent()
        assertTrue(vm.vehicleSignals.value.isStale)
        assertEquals(RideMode.NO_SIGNAL, vm.vehicleSignals.value.rideMode)

        fakeRepo.flow.emit(VehicleRepoImpl.READY)
        runCurrent()
        assertFalse(vm.vehicleSignals.value.isStale)
        assertEquals(RideMode.READY, vm.vehicleSignals.value.rideMode)
    }

}