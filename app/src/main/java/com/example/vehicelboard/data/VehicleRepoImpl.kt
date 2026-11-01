package com.example.vehicelboard.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

class VehicleRepoImpl(
    private val delayMs: Long = 1000L,
    private val hold: Int = 3,
    private val step: Long = 100L,
) : VehicleRepository {

    override val signals: Flow<VehicleSignal> = flow {

        while (true) {
            holdState(PARKED)
            holdState(READY)
            stepUpState(0f, RIDING.speedKmh!!, RIDING)
            holdState(RIDING)
            stepUpState(RIDING.speedKmh!!, ACCELERATING.speedKmh!!, ACCELERATING)
            holdState(ACCELERATING)
            stepDownState(ACCELERATING.speedKmh!!, MOTOR_DISABLED.speedKmh!!, MOTOR_DISABLED)
            holdState(MOTOR_DISABLED)
            holdState(PARKED)
            holdState(MISSING_SPEED)
            holdState(INVALID_SPEED)
        }
    }

    suspend fun FlowCollector<VehicleSignal>.holdState(state: VehicleSignal) {
        repeat(hold) {
            emit(state)
            delay(delayMs.milliseconds)
        }
    }


    suspend fun FlowCollector<VehicleSignal>.stepUpState(
        from: Float,
        to: Float,
        state: VehicleSignal
    ) {
        var speed = from
        while (speed < to) {
            emit(state.copy(speedKmh = speed))
            delay(step.milliseconds)
            speed++
        }
    }

    suspend fun FlowCollector<VehicleSignal>.stepDownState(
        from: Float,
        to: Float,
        state: VehicleSignal
    ) {
        var speed = from
        while (speed > to) {
            emit(state.copy(speedKmh = speed))
            delay(step.milliseconds)
            speed--
        }
    }


    companion object {
        val PARKED = VehicleSignal(speedKmh = 0f, motorOn = false, killSwitchActive = false)
        val READY = VehicleSignal(speedKmh = 0f, motorOn = true, killSwitchActive = false)
        val RIDING = VehicleSignal(speedKmh = 25f, motorOn = true, killSwitchActive = false)
        val ACCELERATING = VehicleSignal(speedKmh = 50f, motorOn = true, killSwitchActive = false)
        val MOTOR_DISABLED = VehicleSignal(speedKmh = 0f, motorOn = false, killSwitchActive = true)

        val INVALID_SPEED = VehicleSignal(speedKmh = -12f, motorOn = true, killSwitchActive = false)
        val MISSING_SPEED = VehicleSignal(speedKmh = null, motorOn = true, killSwitchActive = false)
    }
}