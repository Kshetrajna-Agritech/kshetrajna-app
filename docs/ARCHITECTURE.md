# Android Architecture

## Dependency direction
`UI -> ViewModel -> Use Case -> Repository -> Data Sources`

Data sources: local database, backend/API, BLE.

## UI
Screens, navigation, state rendering. No core business logic or protocol parsing.

## ViewModel
Owns screen state and invokes use cases.

## Domain
Domain models, repository interfaces, approved business rules/use cases. Keep platform-independent where practical.

## Data
Repository implementations, database/API/BLE mappings, persistence and synchronization.

## Recommended layout
```text
kshetrajna-app/
├── app/
├── data/
│   ├── local/
│   ├── remote/
│   ├── ble/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── ui/
│   ├── dashboard/
│   ├── soil/
│   ├── irrigation/
│   ├── fertility/
│   ├── weather/
│   ├── alerts/
│   └── settings/
└── core/
    ├── network/
    ├── bluetooth/
    ├── sync/
    └── utils/
```

Adapt package/module names to the existing project without breaking dependency direction.

## Simulation
Use deterministic simulation before hardware. Simulation must use the same domain interfaces as real sources.

## Safety boundary
`App request -> transport -> firmware validation -> physical actuation`

is not equivalent to `App request -> physical actuation`.
