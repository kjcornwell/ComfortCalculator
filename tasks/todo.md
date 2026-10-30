# Comfort Gauge — todo

Plan: `~/.claude/plans/working-with-new-project-tingly-jellyfish.md`

## Phase 1 — skeleton + model + tests
- [x] git init, reference/ layout, .gitignore, .gitattributes
- [x] Gradle skeleton (AGP 9.5.0, Kotlin 2.4.21, Gradle 9.8.2, compileSdk 37.2, minSdk 26)
- [x] Acceptance tests written first (ISO 7730 D.1, HI, DP, assess, 78°F sweep, units, VM)
- [x] ComfortModel / Units / Gauges ported verbatim
- [x] ComfortViewModel (exact °F source of truth, display-unit slider)
- [x] testDebugUnitTest green (26/26), assembleDebug OK

## Phase 2 — main screen
- [ ] Theme (palette, dark only, no dynamic color, tnum)
- [ ] Header: title, °F/°C segmented toggle, ⓘ
- [ ] Temperature + humidity sliders (gradient tracks, 30dp thumbs, readouts)
- [ ] Hero card with Map tab: cached bitmap (coarse→fine on Default), crosshair, marker, edge arrow, drag sets both
- [ ] Legend row
- [ ] Verdict card (emoji, level, why, meta, tinted background)
- [ ] Four mini gauges
- [ ] Fits 360×640dp with no scroll; content capped at 520dp
- [ ] assembleDebug + emulator screenshot at 65°F / 0%

## Phase 3 — gauge tab, info, persistence, a11y, UI test
- [ ] Big feels-like gauge tab (legend hidden)
- [ ] Info screen: verbatim text, two Canvas charts, RH + dew-point bands, source links, Done/back
- [ ] DataStore persistence (unit, tab)
- [ ] Accessibility: slider stateDescription, map contentDescription, 48dp targets
- [ ] MapDragTest (androidTest) passes
- [ ] Deviation summary + review section

## Review
