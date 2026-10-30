package com.root3.comfortgauge

import com.root3.comfortgauge.model.Gauges
import org.junit.Assert.assertEquals
import org.junit.Test

class GaugesTest {

    @Test
    fun zoneOfUsesExclusiveUpperBound() {
        assertEquals("Cool", Gauges.FEELS.zoneOf(55.0).label)
        assertEquals("Comfortable", Gauges.FEELS.zoneOf(79.9).label)
        assertEquals("Extreme", Gauges.FEELS.zoneOf(200.0).label)
        assertEquals("Extreme", Gauges.FEELS.zoneOf(Double.NaN).label)
        assertEquals("Comfortable", Gauges.DEW.zoneOf(50.5).label)
        assertEquals("Miserable", Gauges.DEW.zoneOf(80.0).label)
    }

    @Test
    fun fracClampsToRange() {
        assertEquals(0.0, Gauges.FEELS.frac(-20.0), 0.0)
        assertEquals(1.0, Gauges.FEELS.frac(150.0), 0.0)
        assertEquals(0.5, Gauges.DEW.frac(45.0), 1e-12)
    }

    @Test
    fun boundaryTicks() {
        assertEquals(listOf(32.0, 55.0, 68.0, 80.0, 90.0, 103.0, 125.0, 0.0, 135.0), Gauges.FEELS.boundaryTicks)
    }
}
