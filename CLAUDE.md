# Project: Comfort Gauge

## Overview

Native Android (Kotlin/Compose) port of the HTML prototype `reference/comfort-gauge.html`. It judges how comfortable an air temperature + relative humidity is, using a thermal axis (ISO 7730 / ASHRAE 55 PMV-PPD) and a humidity axis (RH + dew point).

**`reference/comfort-gauge.html` is the spec.** Every formula, threshold, color and layout decision in it is deliberate; port it exactly. `reference/android-app-prompt.md` is the build brief and holds the acceptance tests.

## Stack

- Language: Kotlin 2.4.21 (AGP built-in Kotlin + `org.jetbrains.kotlin.plugin.compose`)
- Framework: Jetpack Compose (BOM 2026.09.00), Material 3 (dynamic color off, dark only)
- Build: AGP 9.5.0, Gradle 9.8.2 wrapper, version catalog `gradle/libs.versions.toml`
- SDK: compileSdk 37.2, targetSdk 37, minSdk 26. JDK 21 (sdkmanager fails its version check on JDK 25, so set `JAVA_HOME` to jdk-21 when running it)
- Package / applicationId: `com.root3.comfortgauge`
- Allowed libraries: Compose, Material 3, ViewModel, DataStore, test stack. Ask before adding anything else.
- No network, no permissions, no analytics, no ads, no backend.

## GitHub

- Account: `github-personal` (`kjcornwell`)
- Repo: kjcornwell/ComfortCalculator (PUBLIC)
- Ask before every push to `main`.

## Docker

None. This is a mobile app.

## Project Structure

```
reference/                       prototype HTML (spec) + build brief
app/src/main/java/com/root3/comfortgauge/
  model/   ComfortModel.kt (pure Kotlin, no Android imports), Units.kt, Gauges.kt
  data/    SettingsStore (DataStore impl in Phase 3)
  ui/      ComfortViewModel (StateFlow<UiState>), screens/components
app/src/test/        JVM unit + acceptance tests
app/src/androidTest/ Compose UI tests
_docs/               ops logs (Play publishing)
tasks/               todo.md, lessons.md
```

## Testing

```bash
./gradlew.bat testDebugUnitTest --no-daemon        # model + VM acceptance tests
./gradlew.bat assembleDebug --no-daemon
./gradlew.bat connectedDebugAndroidTest --no-daemon  # needs emulator (AVD: Medium_Phone_API_36.0)
```

## Notes

- All math is in °F. The unit (°F/°C) is display-only. `UiState.tempF` is the exact source of truth, so F→C→F never drifts.
- Display rounding uses `Units.jsRound` (JS `Math.round`: halves go toward +∞), not `roundToInt`.
- Verify ported numbers against the prototype: extract its `<script>` math and run it in node.
- Play publishing: see the `play-publish` skill; log every Play operation in `_docs/play-publishing-ops-log.md`.
