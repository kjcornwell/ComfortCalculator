package com.root3.comfortgauge

import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.model.Units
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitsTest {

    @Test
    fun conversions() {
        assertEquals(0.0, Units.f2c(32.0), 1e-12)
        assertEquals(100.0, Units.f2c(212.0), 1e-12)
        assertEquals(71.6, Units.c2f(22.0), 1e-12)
        assertEquals(-40.0, Units.c2f(-40.0), 1e-12)
    }

    @Test
    fun formatInDisplayUnit() {
        assertEquals("65°F", Units.format(65.0, TempUnit.F))
        assertEquals("18°C", Units.format(65.0, TempUnit.C))
        assertEquals("50", Units.format(50.0, TempUnit.F, bare = true))
        assertEquals("10", Units.format(50.0, TempUnit.C, bare = true))
        assertEquals("22°C", Units.format(71.6, TempUnit.C))
    }

    @Test
    fun jsRoundSemantics() {
        assertEquals(3, Units.jsRound(2.5))
        assertEquals(-2, Units.jsRound(-2.5))
        assertEquals(0, Units.jsRound(-0.4))
        assertEquals("0°C", Units.format(31.5, TempUnit.C))
    }

    @Test
    fun formatDelta() {
        assertEquals(10, Units.formatDelta(18.0, TempUnit.C))
        assertEquals(18, Units.formatDelta(18.0, TempUnit.F))
    }
}
