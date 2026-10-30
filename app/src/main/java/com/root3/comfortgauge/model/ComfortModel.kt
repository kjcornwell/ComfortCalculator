package com.root3.comfortgauge.model

import com.root3.comfortgauge.model.Units.c2f
import com.root3.comfortgauge.model.Units.f2c
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

enum class Cause { COLD, HOT, DRY, HUMID, OK }

data class PmvPpd(val pmv: Double, val ppd: Double)

data class Thermal(
    val pmv: Double,
    val ppd: Double,
    val clo: Double,
    val word: String,
    val level: Int,
    val cause: Cause,
)

data class Humidity(val level: Int, val word: String, val cause: Cause)

data class Assessment(
    val thermal: Thermal,
    val humidity: Humidity,
    val level: Int,
    val cause: Cause,
    val color: Long,
)

data class Verdict(
    val tempF: Double,
    val rh: Double,
    val assessment: Assessment,
    val heatIndex: Double,
    val apparent: Double,
    val humidex: Double,
    val dewPoint: Double,
    val feelsLike: Double,
    val emoji: String,
    val levelWord: String,
    val levelColor: Long,
    val why: String,
    val satisfiedPct: Int,
) {
    fun metaLine(unit: TempUnit): String =
        "Feels like ${Units.format(feelsLike, unit)} · $satisfiedPct% of people thermally satisfied"
}

/** Port of reference/comfort-gauge.html. Pure Kotlin, all temperatures in °F unless noted. */
object ComfortModel {

    /** ISO 7730:2005 Annex D. Air/radiant temperature in °C, velocity m/s, RH %. */
    fun pmvPpd(ta: Double, tr: Double, vel: Double, rh: Double, met: Double, clo: Double): PmvPpd {
        val pa = rh * 10 * exp(16.6536 - 4030.183 / (ta + 235))
        val icl = 0.155 * clo
        val m = met * 58.15
        val mw = m
        val fcl = if (icl <= 0.078) 1 + 1.29 * icl else 1.05 + 0.645 * icl
        val hcf = 12.1 * sqrt(vel)
        val taa = ta + 273
        val tra = tr + 273
        val tcla = taa + (35.5 - ta) / (3.5 * icl + 0.1)
        val p1 = icl * fcl
        val p2 = p1 * 3.96
        val p3 = p1 * 100
        val p4 = p1 * taa
        val p5 = 308.7 - 0.028 * mw + p2 * (tra / 100).pow(4)
        var xn = tcla / 100
        var xf = tcla / 50
        var hc = hcf
        var n = 0
        while (abs(xn - xf) > 0.00015) {
            xf = (xf + xn) / 2
            val hcn = 2.38 * abs(100 * xf - taa).pow(0.25)
            hc = if (hcf > hcn) hcf else hcn
            xn = (p5 + p4 * hc - p2 * xf.pow(4)) / (100 + p3 * hc)
            if (++n > 150) break
        }
        val tcl = 100 * xn - 273
        val hl1 = 3.05e-3 * (5733 - 6.99 * mw - pa)
        val hl2 = if (mw > 58.15) 0.42 * (mw - 58.15) else 0.0
        val hl3 = 1.7e-5 * m * (5867 - pa)
        val hl4 = 0.0014 * m * (34 - ta)
        val hl5 = 3.96 * fcl * (xn.pow(4) - (tra / 100).pow(4))
        val hl6 = fcl * hc * (tcl - ta)
        val ts = 0.303 * exp(-0.036 * m) + 0.028
        val v = ts * (mw - hl1 - hl2 - hl3 - hl4 - hl5 - hl6)
        return PmvPpd(v, 100 - 95 * exp(-0.03353 * v.pow(4) - 0.2179 * v.pow(2)))
    }

    /**
     * Best case over clothing 0.5–1.0 clo (ASHRAE 55 summer/winter union). PMV is monotone in clo
     * across the range, so evaluate both ends and interpolate the zero crossing if there is one.
     */
    fun thermal(tF: Double, rh: Double): Thermal {
        val t = f2c(tF)
        val rhc = max(rh, 0.5)
        val a = pmvPpd(t, t, 0.1, rhc, 1.1, 0.5)
        val b = pmvPpd(t, t, 0.1, rhc, 1.1, 1.0)
        val best: PmvPpd
        val clo: Double
        if (a.pmv * b.pmv < 0) {
            clo = 0.5 + 0.5 * (-a.pmv) / (b.pmv - a.pmv)
            best = pmvPpd(t, t, 0.1, rhc, 1.1, clo)
        } else if (abs(a.pmv) <= abs(b.pmv)) {
            clo = 0.5; best = a
        } else {
            clo = 1.0; best = b
        }
        val p = best.pmv
        val word = when {
            p <= -2.5 -> "Cold"
            p <= -1.5 -> "Cool"
            p <= -0.5 -> "Slightly cool"
            p < 0.5 -> "Neutral"
            p < 1.5 -> "Slightly warm"
            p < 2.5 -> "Warm"
            else -> "Hot"
        }
        // ISO 7730 cat. B / C, then widening
        val ppd = best.ppd
        val level = when {
            ppd <= 10 -> 0
            ppd <= 15 -> 1
            ppd <= 25 -> 2
            ppd <= 50 -> 3
            else -> 4
        }
        return Thermal(p, ppd, clo, word, level, if (p < 0) Cause.COLD else Cause.HOT)
    }

    /** Non-thermal humidity axis. [td] is dew point in °F. */
    fun humidity(rh: Double, td: Double): Humidity = when {
        rh < 10 -> Humidity(4, "Extremely dry", Cause.DRY)
        rh < 20 -> Humidity(3, "Very dry", Cause.DRY)
        rh < 30 -> Humidity(2, "Dry", Cause.DRY)
        td > 70 -> Humidity(4, "Oppressive", Cause.HUMID)
        td > 65 -> Humidity(3, "Muggy", Cause.HUMID)
        td > 62 -> Humidity(2, "Humid", Cause.HUMID) // ASHRAE 55: 0.012 kg/kg ≈ 62°F dew point
        rh > 75 -> Humidity(3, "Damp", Cause.HUMID)
        rh > 65 -> Humidity(2, "Humid", Cause.HUMID)
        rh < 40 -> Humidity(1, "Slightly dry", Cause.DRY)
        rh > 60 || td > 60 -> Humidity(1, "Slightly humid", Cause.HUMID)
        else -> Humidity(0, "Ideal humidity", Cause.OK)
    }

    /** NWS Rothfusz regression with adjustments; below 80°F returns air temperature. */
    fun heatIndex(t: Double, rh: Double): Double {
        if (t < 80) return t
        var hi = -42.379 + 2.04901523 * t + 10.14333127 * rh - 0.22475541 * t * rh -
            6.83783e-3 * t * t - 5.481717e-2 * rh * rh + 1.22874e-3 * t * t * rh +
            8.5282e-4 * t * rh * rh - 1.99e-6 * t * t * rh * rh
        if (rh < 13 && t <= 112) hi -= ((13 - rh) / 4) * sqrt((17 - abs(t - 95)) / 17)
        else if (rh > 85 && t <= 87) hi += ((rh - 85) / 10) * ((87 - t) / 5)
        return hi
    }

    /** Magnus formula, Alduchov–Eskridge coefficients. RH floored at 0.5%. */
    fun dewPoint(t: Double, rh: Double): Double {
        val tc = f2c(t)
        val a = 17.625
        val b = 243.04
        val g = ln(max(rh, 0.5) / 100) + a * tc / (b + tc)
        return c2f(b * g / (a - g))
    }

    /** BOM apparent temperature, wind = 0. */
    fun apparentTemp(t: Double, rh: Double): Double {
        val tc = f2c(t)
        val e = (rh / 100) * 6.105 * exp(17.27 * tc / (237.7 + tc))
        return c2f(tc + 0.33 * e - 4.00)
    }

    /** Environment Canada humidex. */
    fun humidex(t: Double, rh: Double): Double {
        val tc = f2c(t)
        val td = f2c(dewPoint(t, rh))
        val e = 6.11 * exp(5417.7530 * (1 / 273.16 - 1 / (273.16 + td)))
        return c2f(tc + 0.5555 * (e - 10))
    }

    val RAMP: Map<Cause, List<Long>> = mapOf(
        Cause.COLD to listOf(0xFF93C5FD, 0xFF3B82F6, 0xFF1D4ED8),
        Cause.HOT to listOf(0xFFFDE047, 0xFFF97316, 0xFFDC2626),
        Cause.DRY to listOf(0xFFD9A066, 0xFFB26A2E, 0xFF7A3D10),
        Cause.HUMID to listOf(0xFFC4B5FD, 0xFF8B5CF6, 0xFF5B21B6),
    )
    val OK: List<Long> = listOf(0xFF16A34A, 0xFF84CC16)
    val LEVEL_WORDS = listOf("Ideal", "Good", "Marginal", "Poor", "Bad")
    private val EMOJI = mapOf(
        Cause.COLD to "🥶", Cause.HOT to "🥵", Cause.DRY to "🏜️", Cause.HUMID to "💦",
    )
    private val LEVEL_EMOJI = listOf("😊", "🙂", "😐", "😬")

    fun ramp(cause: Cause): List<Long> =
        RAMP[cause] ?: throw IllegalArgumentException("No color ramp for cause $cause")

    fun assess(tF: Double, rh: Double): Assessment {
        val th = thermal(tF, rh)
        val hu = humidity(rh, dewPoint(tF, rh))
        val level = max(th.level, hu.level)
        val cause = when {
            level <= 1 -> Cause.OK
            th.level >= hu.level -> th.cause
            else -> hu.cause
        }
        val color = if (level <= 1) OK[level] else ramp(cause)[level - 2]
        return Assessment(th, hu, level, cause, color)
    }

    fun verdict(tF: Double, rh: Double): Verdict {
        val a = assess(tF, rh)
        val hi = heatIndex(tF, rh)
        val at = apparentTemp(tF, rh)
        val hx = humidex(tF, rh)
        val dp = dewPoint(tF, rh)
        val emoji = if (a.level <= 3) LEVEL_EMOJI[a.level] else EMOJI.getValue(a.cause)
        return Verdict(
            tempF = tF,
            rh = rh,
            assessment = a,
            heatIndex = hi,
            apparent = at,
            humidex = hx,
            dewPoint = dp,
            feelsLike = (hi + at + hx) / 3,
            emoji = emoji,
            levelWord = LEVEL_WORDS[a.level],
            levelColor = if (a.level <= 1) a.color else ramp(a.cause)[0],
            why = "${a.thermal.word} · ${a.humidity.word}",
            satisfiedPct = Units.jsRound(100 - a.thermal.ppd),
        )
    }
}
