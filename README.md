# Vehicle Ride Dashboard

Landscape Android dashboard (Kotlin + Jetpack Compose) showing simulated speed (km/h), motor ON/OFF and kill-switch Active/Inactive. No hardware, CAN or backend: a coroutine-driven simulator plays the part of the vehicle.

## Build and Run
Requirements: Android Studio Rabbit 1 (2026.2.1) or newer, JDK 17, an emulator or device with API 26+
1. Open the HmiDashboard folder in Android Studio and let Gradle sync.
2. Pick any emulator (e.g. Pixel Tablet, or a phone: the activity is locked to landscape) and press Run.
3. Unit tests: ./gradlew :app:testDebugUnitTest or right-click app/src/test -> Run Tests

## Architecture

The project follows the **MVVM (Model-View-ViewModel)** pattern combined with **Clean Architecture** principles and Unidirectional Data Flow (UDF) powered by Kotlin Coroutines & Flow.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                 UI Layer                                    │
│  [MainActivity] -> [DashboardScreen] <─ (Collect State) ─ [DashBoardViewModel] │
└─────────────────────────────────────┬───────────────────────────────────────┘
                                      │ StateFlow<VehicleUiState>
┌─────────────────────────────────────▼───────────────────────────────────────┐
│                              Domain UseCase Layer                           │
│                              [VehicleUseCase]                               │
│                Validates signals & maps to VehicleUiState                   │
└─────────────────────────────────────┬───────────────────────────────────────┘
                                      │ Flow<VehicleSignal>
┌─────────────────────────────────────▼───────────────────────────────────────┐
│                                 Data Layer                                  │
│                      [VehicleRepository] (Interface)                        │
│                                     ▲                                       │
│                             [VehicleRepoImpl]                               │
│                  Simulates real-time telemetry stream                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Layer Breakdown

1. **Data Layer (`com.example.vehicelboard.data`)**
   - **`VehicleSignal`**: Data class holding raw telemetry fields (`speedKmh: Float?`, `motorOn: Boolean?`, `killSwitchActive: Boolean?`).
   - **`VehicleRepository`**: Interface contract exposing `val signals: Flow<VehicleSignal>`.
   - **`VehicleRepoImpl`**: Coroutine-based implementation emitting real-time vehicle telemetry signals over `Flow`. Simulates nominal states (`PARKED`, `READY`, `RIDING`, `ACCELERATING`, `MOTOR_DISABLED`) and error scenarios (`MISSING_SPEED`, `INVALID_SPEED`).

2. **Domain / UseCase Layer (`com.example.vehicelboard.usecase`)**
   - **`VehicleUseCase`**: Transforms raw `VehicleSignal` telemetry into a validated `VehicleUiState`. Rejects invalid speeds (`< 0` or non-finite numbers), handles `null` values gracefully, and maps raw flags to domain status enums (`SpeedStatus`, `MotorStatus`, `KillSwitchStatus`).
   - **`VehicleUiState`**: Immutable UI state containing telemetry values and a computed property `rideMode` (`PARKED`, `READY`, `RIDING`, `ACCELERATING`, `COASTING`, `MOTOR_DISABLED`, `NO_SIGNAL`).

3. **ViewModel Layer (`com.example.vehicelboard.ui.viewmodel`)**
   - **`DashBoardViewModel`**: Manages state lifecycle and exposes `vehicleSignals: StateFlow<VehicleUiState>` to the UI.
   - **Stale Connection Handling**: Uses `transformLatest` with a 3000ms delay timeout to emit `timeoutState()` if telemetry halts. Uses `.catch` to handle upstream exceptions safely.
   - **ViewModel Factory**: Provides `DashBoardViewModel.Factory` (`ViewModelProvider.Factory`) for flexible instantiation and dependency injection.

4. **UI Layer (`com.example.vehicelboard.ui.screens`)**
   - **`DashboardScreen`**: Main screen composable retrieving `DashBoardViewModel` and collecting states using `collectAsStateWithLifecycle()`.
   - **`DashBoardContent` & `Components`**: Modular Jetpack Compose UI rendering speed, ride mode header, motor status card, and kill switch status card with dynamic state color animations (`animateColorAsState`).

## Simulated sequence (loops forever, one emission per second)
Each state is held for 3 seconds (`holdTicks`):

| # | Scenario | Speed | Motor | Kill switch | Header label |
|---|----------|-------|-------|-------------|--------------|
| 1 | Parked | 0 | OFF | Inactive | PARKED |
| 2 | Ready | 0 | ON | Inactive | READY |
| 3 | Riding | 25 | ON | Inactive | RIDING |
| 4 | Accelerating | 50 | ON | Inactive | ACCELERATING |
| 5 | Motor disabled | 0 | OFF | **Active** | MOTOR DISABLED |

Then the negative and null Scenarios:
1. Speed `-12` -> "Invalid speed reading ignored", speed shows `--`, label Connection lost.Waiting for signal.
2. Speed `null` -> "Speed signal unavailable".
3. 5s of silence -> after 3s without data the screen switches to NO SIGNAL. The next emission recovers automatically.

## Edge cases handled
* **Invalid speed**: negative, NaN, infinite is rejected (`SpeedStatus.INVALID`) and shown as `--` with a message.
* **Missing signal**: any null signal becomes `UNAVAILABLE` / `UNKNOWN`, never a default value.
* **Stale data**: `transformLatest` + `delay` in the ViewModel marks data stale if nothing arrives within 3s, replacing all values with `--`.
* **Source failure**: `.catch` turns an exception in the data source into the NO SIGNAL state instead of a crash.

## Tests (`app/src/test`)
* `VehicleUiStateMapper`: Three scenarios map to the right modes; missing signals; stale masking.
*  `DashBoardViewModelTest`: state updates through all five scenarios; invalid speed; stale timeout and recovery (virtual time); simulator emits the sequence at 1s intervals.