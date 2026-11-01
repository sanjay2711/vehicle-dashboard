package com.example.vehicelboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.vehicelboard.usecase.VehicleUiState
import com.example.vehicelboard.usecase.VehicleUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlin.time.Duration.Companion.milliseconds

class DashBoardViewModel @JvmOverloads constructor(
    vehicleUseCase: VehicleUseCase = VehicleUseCase(),
    private val timeOut: Long = 3000L
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val vehicleSignals = vehicleUseCase()
        .onStart {
            emit(VehicleUiState.Initial)
        }
        .transformLatest { state ->
            emit(state)
            delay(timeOut.milliseconds)
            emit(state.timeoutState())
        }
        .catch {
            emit(VehicleUiState.Initial.timeoutState())
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            VehicleUiState.Initial
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DashBoardViewModel(
                    vehicleUseCase = VehicleUseCase()
                )
            }
        }
    }
}

