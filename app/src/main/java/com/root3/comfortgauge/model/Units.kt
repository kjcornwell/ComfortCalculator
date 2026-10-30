package com.root3.comfortgauge.model

import kotlin.math.floor

enum class TempUnit { F, C }

/** All math is in °F; the unit only affects display. */
object Units {
    fun f2c(f: Double): Double = (f - 32) * 5 / 9
    fun c2f(c: Double): Double = c * 9 / 5 + 32

    /** JavaScript Math.round: halves round toward +∞. */
    fun jsRound(x: Double): Int = floor(x + 0.5).toInt()

    /** °F → display unit. */
    fun display(f: Double, unit: TempUnit): Double = if (unit == TempUnit.C) f2c(f) else f

    fun format(f: Double, unit: TempUnit, bare: Boolean = false): String =
        jsRound(display(f, unit)).toString() + if (bare) "" else "°${unit.name}"

    /** For values that are temperature differences, not absolute temperatures. */
    fun formatDelta(f: Double, unit: TempUnit): Int = jsRound(if (unit == TempUnit.C) f * 5 / 9 else f)
}
