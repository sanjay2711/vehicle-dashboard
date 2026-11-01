package com.example.vehicelboard.data

import kotlinx.coroutines.flow.Flow

data class VehicleSignal(
    var speedKmh : Float?,
    var motorOn : Boolean?,
    var killSwitchActive : Boolean?
)

interface VehicleRepository {
    val signals : Flow<VehicleSignal>
}

