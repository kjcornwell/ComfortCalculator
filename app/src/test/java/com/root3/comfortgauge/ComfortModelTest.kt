package com.root3.comfortgauge

import com.root3.comfortgauge.model.Cause
import com.root3.comfortgauge.model.ComfortModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComfortModelTest {

    // ISO 7730:2005 Table D.1
    private data class IsoRow(
        val ta: Double, val tr: Double, val vel: Double, val rh: Double,
        val met: Double, val clo: Double, val pmv: Double, val ppd: Double,
    )

    private val isoRows = listOf(
        IsoRow(22.0, 22.0, 0.10, 60.0, 1.2, 0.5, -0.75, 17.0),
        IsoRow(27.0, 27.0, 0.10, 60.0, 1.2, 0.5, 0.77, 17.0),
        IsoRow(27.0, 27.0, 0.30, 60.0, 1.2, 0.5, 0.44, 9.0),
        IsoRow(23.5, 25.5, 0.10, 60.0, 1.2, 0.5, -0.01, 5.0),
    )

    @Test
    fun pmvPpd_matchesIso7730TableD1() {
        for (r in isoRows) {
            val p = ComfortModel.pmvPpd(r.ta, r.tr, r.vel, r.rh, r.met, r.clo)
            assertEquals("PMV for $r", r.pmv, p.pmv, 0.02)
            assertEquals("PPD for $r", r.ppd, p.ppd, 1.0)
        }
    }

    @Test
    fun heatIndex_matchesNws() {
        assertEquals(99.7, ComfortModel.heatIndex(90.0, 60.0), 0.5)
        assertEquals(133.8, ComfortModel.heatIndex(95.0, 80.0), 0.5)
        assertEquals(70.0, ComfortModel.heatIndex(70.0, 50.0), 0.5)
    }

    @Test
    fun heatIndex_below80ReturnsAirTemp() {
        assertEquals(70.0, ComfortModel.heatIndex(70.0, 50.0), 0.0)
        assertEquals(79.0, ComfortModel.heatIndex(79.0, 100.0), 0.0)
    }

    @Test
    fun dewPoint_magnus() {
        assertEquals(50.5, ComfortModel.dewPoint(70.0, 50.0), 0.5)
        assertEquals(0.0, ComfortModel.dewPoint(0.0, 100.0), 0.5)
    }

    @Test
    fun dewPoint_zeroHumidityIsFinite() {
        for (t in listOf(0.0, 32.0, 65.0, 100.0, 125.0)) {
            val dp = ComfortModel.dewPoint(t, 0.0)
            assertTrue("dewPoint($t, 0) = $dp", dp.isFinite())
        }
    }

    @Test
    fun assess_65F_0RH_isBadDry() {
        val a = ComfortModel.assess(65.0, 0.0)
        assertEquals(4, a.level)
        assertEquals(Cause.DRY, a.cause)
        assertEquals("Slightly cool", a.thermal.word)
        assertEquals("Extremely dry", a.humidity.word)
    }

    @Test
    fun assess_75F_50RH_isIdeal() {
        val a = ComfortModel.assess(75.0, 50.0)
        assertEquals(0, a.level)
        assertEquals("Neutral", a.thermal.word)
        assertEquals("Ideal humidity", a.humidity.word)
    }

    @Test
    fun assess_72F_35RH_isGood() {
        val a = ComfortModel.assess(72.0, 35.0)
        assertEquals(1, a.level)
        assertEquals("Neutral", a.thermal.word)
        assertEquals("Slightly dry", a.humidity.word)
    }

    @Test
    fun assess_90F_60RH_isBadHot() {
        val a = ComfortModel.assess(90.0, 60.0)
        assertEquals(4, a.level)
        assertEquals(Cause.HOT, a.cause)
    }

    @Test
    fun assess_78F_humidityBarelyMovesThermalPpd() {
        for (rh in 0..100) {
            val ppd = ComfortModel.assess(78.0, rh.toDouble()).thermal.ppd
            assertTrue("PPD at 78°F/$rh% = $ppd", ppd in 4.0..10.0)
        }
    }

    @Test
    fun assess_colorFollowsLevelAndCause() {
        assertEquals(ComfortModel.OK[0], ComfortModel.assess(75.0, 50.0).color)
        assertEquals(ComfortModel.OK[1], ComfortModel.assess(72.0, 35.0).color)
        assertEquals(ComfortModel.ramp(Cause.DRY)[2], ComfortModel.assess(65.0, 0.0).color)
        assertEquals(ComfortModel.ramp(Cause.HOT)[2], ComfortModel.assess(90.0, 60.0).color)
    }

    @Test
    fun verdict_65F_0RH() {
        val v = ComfortModel.verdict(65.0, 0.0)
        assertEquals("Bad", v.levelWord)
        assertEquals("🏜️", v.emoji)
        assertEquals("Slightly cool · Extremely dry", v.why)
        assertEquals(64, v.satisfiedPct)
        assertEquals(ComfortModel.ramp(Cause.DRY)[0], v.levelColor)
    }
}
