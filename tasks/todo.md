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
- [x] Theme (palette, dark only, no dynamic color, tnum)
- [x] Header: title, °F/°C segmented toggle, ⓘ
- [x] Temperature + humidity sliders (gradient tracks, 30dp thumbs, readouts)
- [x] Hero card with Map tab: cached bitmap (coarse→fine on Default), crosshair, marker, edge arrow, drag sets both
- [x] Legend row
- [x] Verdict card (emoji, level, why, meta, tinted background)
- [x] Four mini gauges
- [x] Fits 360×640dp with no scroll; content capped at 520dp
- [x] assembleDebug + emulator screenshot at 65°F / 0%

## Phase 3 — gauge tab, info, persistence, a11y, UI test
- [x] Big feels-like gauge tab (legend hidden)
- [x] Info screen: verbatim text, two Canvas charts, RH + dew-point bands, source links, Done/back
- [x] DataStore persistence (unit, tab)
- [x] Accessibility: slider stateDescription, map contentDescription, 48dp targets
- [x] MapDragTest (androidTest) passes
- [ ] Deviation summary + review section

## Review
