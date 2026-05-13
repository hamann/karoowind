# karoowind

A [Hammerhead Karoo 3](https://www.hammerhead.io/) extension that controls the [Wahoo Headwind](https://www.wahoofitness.com/devices/accessories/kickr-headwind) smart fan over Bluetooth LE.

## Features

- **Auto mode** — fan speed adjusts automatically based on heart rate zones
- **Manual mode** — increase/decrease fan speed with hardware buttons
- **Ride screen tile** — shows current fan speed (%) and AUTO/MANUAL mode

## Heart rate → fan speed mapping

| Heart rate | Fan speed |
|---|---|
| < 110 bpm | Off (0%) |
| 110–129 bpm | Low (30%) |
| 130–149 bpm | Medium (60%) |
| ≥ 150 bpm | Full (100%) |

Pressing a manual speed button switches to MANUAL mode for the rest of the session.

## Development setup

### Prerequisites

- [Nix](https://nixos.org/download/) with flakes enabled
- A [GitHub personal access token](https://github.com/settings/tokens) (classic, `read:packages` scope) — required to download the `karoo-ext` SDK from GitHub Packages

### Enter the dev environment

```sh
nix develop
# or, with direnv:
direnv allow
```

This provides JDK 17, Gradle, and the Android SDK (build tools 34).

### Configure credentials

```sh
cp local.properties.example local.properties
```

Edit `local.properties` with your GitHub credentials:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

`local.properties` is gitignored and never committed.

### Build

```sh
gradle assembleDebug
# APK output: app/build/outputs/apk/debug/app-debug.apk
```

## Sideloading onto Karoo 3

### One-time device setup

1. On the Karoo: **Settings → About → tap Firmware Version 7 times** to unlock Developer Options
2. **Settings → Developer Options → enable USB Debugging**

### Install via USB

```sh
# Verify the Karoo is detected
adb devices

# Install
adb install app/build/outputs/apk/debug/app-debug.apk

# Or build and install in one step
gradle installDebug
```

Accept the **Allow USB Debugging** prompt on the Karoo screen the first time.

### View logs

```sh
adb logcat -s KarooWind:* KarooExtension:* KarooSystem:*
```

## Usage on the Karoo

1. Add the **Headwind Speed** data field to a ride page
2. Assign the **Fan Speed Up** and **Fan Speed Down** bonus actions to hardware buttons in the Karoo extension settings
3. Start a ride — the fan will follow your heart rate automatically
4. Press the assigned buttons to switch to manual control

## Project structure

```
app/src/main/kotlin/dev/hamann/karoowind/
├── KarooWindExtension.kt   # Karoo extension service entry point
├── HeadwindManager.kt      # BLE connection and fan speed control
├── HeadwindDataType.kt     # Ride screen tile (data type implementation)
├── HeadwindView.kt         # Glance composable for the tile UI
└── Extensions.kt           # streamDataFlow / consumerFlow helpers
```

## BLE protocol

The Wahoo Headwind communicates over BLE. The service and characteristic UUIDs in `HeadwindManager.kt` are based on community reverse engineering — verify them against your device before use.

| | UUID |
|---|---|
| Service | `a026ee0b-0a7d-4ab3-97fa-f1500f9feb8b` |
| Fan speed characteristic | `a026e038-0a7d-4ab3-97fa-f1500f9feb8b` |

Fan speed is written as a single byte (0–100).

## Dependencies

| Dependency | Purpose |
|---|---|
| [`io.hammerhead:karoo-ext`](https://github.com/hammerheadnav/karoo-ext) | Karoo Extensions SDK |
| `androidx.glance:glance-appwidget` | Ride screen tile UI |
| `kotlinx.coroutines` | Async BLE and data streaming |
| `timber` | Logging |
