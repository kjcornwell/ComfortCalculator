# Claude Code prompt — Comfort Gauge for Android

> Paste everything below the line into Claude Code, run from an empty directory that contains `reference/comfort-gauge.html` (the HTML prototype). The prototype is the spec; this prompt tells Claude Code how to turn it into a native app and how to prove it got the math right.

---

Build a native Android app called **Comfort Gauge** from the HTML prototype at `reference/comfort-gauge.html`. Read the entire HTML file first — every formula, threshold, color and layout decision in it is deliberate and the app must reproduce them exactly. Do not open it in a WebView; this is a native Compose port.

## What the app does

The user sets an air temperature and a relative humidity. The app says how comfortable that air is for most people and *why*, by judging two independent axes and combining them:

1. **Thermal axis** — ISO 7730 / ASHRAE 55 PMV-PPD model (Fanger). Predicts the % of people dissatisfied. Best case over clothing 0.5–1.0 clo, seated/light activity (1.1 met), still air (0.1 m/s), mean radiant temp = air temp.
2. **Humidity axis** — non-thermal, categorical: dry side by relative humidity (<10 / <20 / <30 / <40 %), wet side by dew point (>62 / >65 / >70 °F, the first being ASHRAE 55's 0.012 kg/kg limit) and by RH for cold-damp (>65 / >75 %). 40–60% RH with dew point ≤60°F is ideal.

Overall level = the worse of the two (0 Ideal, 1 Good, 2 Marginal, 3 Poor, 4 Bad). The *cause* (cold / hot / dry / humid) is whichever axis is worse, thermal winning ties; it picks the color ramp.

Four "feels-like" indices are also shown as small gauges: NWS Heat Index, BOM Apparent Temperature (wind = 0), Environment Canada Humidex, and dew point (Magnus, Alduchov-Eskridge coefficients). "Feels like" in the verdict is the mean of the first three.

## Screens and components (match the prototype)

**Main screen**, one viewport, no scrolling, in this vertical order:
- Header: title, a °F/°C segmented toggle, and an ⓘ button.
- Two sliders with large current-value readouts: temperature 0–125°F (−18–52°C in C mode, 1° steps in the displayed unit) and humidity 0–100%. The temperature track is a cold→hot color gradient; the humidity track is dry-brown → green → humid-purple.
- Hero card with **Map / Gauge** tabs (Map is default; remember the last choice):
  - **Map**: a 2-D comfort map, temperature 50–100°F on x, humidity 0–100% on y, every cell colored by `assess(T,RH).color`. Smooth zone edges (sample at 0.25°F × 0.5% and draw with bilinear filtering). Dashed crosshair and a white marker dot with a colored center at the current point. **Dragging anywhere on the map sets both values at once.** When the temperature is outside 50–100°F the marker pins to the edge with a small arrow pointing outward. Axis ticks every 10°F or every 5°C depending on unit. Legend row beneath: Ideal, Good, then Cold/Hot/Dry/Humid each with its 3-step ramp, and the note "lighter → darker = marginal → poor → bad".
  - **Gauge**: a large half-circle arc on the 0–135°F "feels like" band with zone colors, tick labels at the zone boundaries, the marker dot riding the arc, the current emoji inside, the feels-like value, and a "FEELS LIKE" caption.
- Verdict card: emoji, level word (colored), "Thermal word · Humidity word" (e.g. "Slightly cool · Extremely dry"), and a meta line "Feels like 59°F · 64% of people thermally satisfied". The card background is tinted with the level color.
- Row of four mini gauges (Heat Index, Apparent, Humidex, Dew Point): half-circle arc with zone bands, marker dot, value in the display unit, zone label in the zone color underneath.

**Info screen** (full-screen, scrollable, opened by ⓘ, closed by Done): port the prototype's info page content verbatim — what the tool does, the map explanation, the thermal axis section with the *"% dissatisfied vs temperature"* chart (three clothing lines, 50% RH, ASHRAE 10% limit line), the *"% dissatisfied vs humidity at 65/78/90°F"* chart, the RH band bar, the dew-point band bar, the four indices explained, assumptions & limits, and the sources list with tappable links. Charts are drawn natively with Compose Canvas from the same model code at runtime, not embedded images, so they re-label themselves when the unit changes.

**Units**: all math is done in °F. The unit is display-only and converts every visible temperature: slider range and readout, map ticks, every gauge value and tick label, the verdict, the info charts' axis labels and series names, and the inline numbers in the info text. Keep the exact °F value as the source of truth so toggling °F→°C→°F returns the identical value. Persist unit and tab choice with DataStore.

**Visual design**: dark theme only for now, matching the prototype's palette (`#0b1220` background, `#131c2e` cards, `#e5e7eb` text, `#8b97ad` muted) and the exact zone/ramp hex values in the HTML. Rounded 12–14dp cards, 30dp slider thumbs, tabular numerals for all readouts. Disable Material dynamic color. Must fit without scrolling on a 360×640dp phone and look right on a 6.7" phone and a tablet (cap content width at ~520dp, centered).

## Tech constraints

- Kotlin, Jetpack Compose, Material 3, single `:app` module, Gradle Kotlin DSL with a version catalog. minSdk 26, targetSdk/compileSdk = current stable.
- Architecture: `ComfortModel` is a pure-Kotlin object with no Android imports (all formulas, thresholds, colors as hex ints, and the `assess()` function). A `ComfortViewModel` holds `StateFlow<UiState>` (tempF: Double, rh: Int, unit, tab). Composables are stateless and take state + callbacks.
- Comfort map: compute the color grid on `Dispatchers.Default` into an `ImageBitmap`; paint a coarse 1°F × 2% grid first so the UI is never blank, then swap in the 0.25°F × 0.5% grid. Cache it; it only depends on the model, not on the slider position. Draw with `FilterQuality.High`.
- Port the thermal shortcut from the HTML: evaluate PMV at 0.5 and 1.0 clo, interpolate the zero crossing if the signs differ, otherwise take the smaller |PMV|. Don't scan six clothing values.
- No network, no permissions, no analytics, no ads, no backend.
- Accessibility: sliders expose value text in the display unit; the map has a content description that reads the current verdict; minimum 48dp touch targets on the toggle, tabs and ⓘ.

## Acceptance tests (write these as unit tests before porting the UI; they must pass)

ISO 7730 Table D.1 — `pmvppd(ta, tr, vel, rh, met, clo)`, air/radiant in °C:
| ta | tr | vel | rh | met | clo | PMV | PPD |
|---|---|---|---|---|---|---|---|
| 22 | 22 | 0.10 | 60 | 1.2 | 0.5 | −0.75 | 17 |
| 27 | 27 | 0.10 | 60 | 1.2 | 0.5 | 0.77 | 17 |
| 27 | 27 | 0.30 | 60 | 1.2 | 0.5 | 0.44 | 9 |
| 23.5 | 25.5 | 0.10 | 60 | 1.2 | 0.5 | −0.01 | 5 |
(tolerance ±0.02 PMV, ±1 PPD)

Indices (°F in, °F out, tolerance ±0.5):
- `heatIndex(90, 60)` ≈ 99.7 · `heatIndex(95, 80)` ≈ 133.8 · `heatIndex(70, 50)` = 70 (below 80°F returns air temp)
- `dewPoint(70, 50)` ≈ 50.5 · `dewPoint(0, 100)` = 0 · `dewPoint(T, 0)` must not throw (RH is floored at 0.5%)

`assess(T, RH)` end to end:
- `assess(65, 0)` → level 4 (Bad), cause `dry`, thermal word "Slightly cool", humidity word "Extremely dry"
- `assess(75, 50)` → level 0 (Ideal), "Neutral", "Ideal humidity"
- `assess(72, 35)` → level 1 (Good), "Neutral", "Slightly dry"
- `assess(90, 60)` → level 4 (Bad), cause `hot`
- `assess(78, RH)` for RH in 0..100 → thermal PPD stays between 4 and 10 (humidity barely moves the thermal number at room temperature)

Unit handling:
- Setting 65°F, toggling to °C and back three times still reads 65°F.
- In °C mode the slider is −18..52 with step 1; setting 22°C reads "22°C" and `assess` receives 71.6°F.

Also add a Compose UI test that drags on the map and asserts both sliders changed.

## How to work

1. Read `reference/comfort-gauge.html` completely and list, in a short plan, every function, threshold table and color you will port. Ask me only if something in the HTML is genuinely ambiguous; otherwise port it as-is.
2. Phase 1: project skeleton + `ComfortModel` + all unit tests green. Stop and show me the test output.
3. Phase 2: main screen with sliders, map, verdict, mini gauges, unit toggle. Must build with `./gradlew assembleDebug` and run on an emulator. Show me a screenshot at 65°F / 0%.
4. Phase 3: Gauge tab, info screen with charts, persistence, accessibility pass, UI tests.
5. Keep commits small and descriptive. Don't add libraries beyond Compose, Material 3, ViewModel, DataStore and the test stack without saying why.

When you are done, give me a one-paragraph summary of anything you had to deviate from the prototype on and why.
