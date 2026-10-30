package com.root3.comfortgauge

import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.ui.ComfortViewModel
import com.root3.comfortgauge.ui.Tab
import org.junit.Assert.assertEquals
import org.junit.Test

class ComfortViewModelTest {

    @Test
    fun defaultsMatchPrototype() {
        val s = ComfortViewModel().state.value
        assertEquals(75.0, s.tempF, 0.0)
        assertEquals(50, s.rh)
        assertEquals(TempUnit.F, s.unit)
        assertEquals(Tab.MAP, s.tab)
    }

    @Test
    fun unitRoundTripPreservesExactFahrenheit() {
        val vm = ComfortViewModel()
        vm.setTempF(65.0)
        repeat(3) {
            vm.setUnit(TempUnit.C)
            vm.setUnit(TempUnit.F)
        }
        assertEquals(65.0, vm.state.value.tempF, 0.0)
        assertEquals("65°F", vm.state.value.tempLabel)
    }

    @Test
    fun celsiusSliderRangeAndStep() {
        val vm = ComfortViewModel()
        vm.setUnit(TempUnit.C)
        val s = vm.state.value
        assertEquals(-18, s.sliderRange.first)
        assertEquals(52, s.sliderRange.last)
        assertEquals(1, s.sliderRange.step)
        vm.setUnit(TempUnit.F)
        assertEquals(0..125, vm.state.value.sliderRange)
    }

    @Test
    fun celsiusSliderFeedsFahrenheitToAssess() {
        val vm = ComfortViewModel()
        vm.setUnit(TempUnit.C)
        vm.setTempFromSlider(22)
        val s = vm.state.value
        assertEquals("22°C", s.tempLabel)
        assertEquals(22, s.sliderValue)
        assertEquals(71.6, s.tempF, 1e-9)
        assertEquals(71.6, s.verdict.tempF, 1e-9)
    }

    @Test
    fun celsiusSliderClampsToFahrenheitRange() {
        val vm = ComfortViewModel()
        vm.setUnit(TempUnit.C)
        vm.setTempFromSlider(-18)
        assertEquals(0.0, vm.state.value.tempF, 0.0)
        vm.setTempFromSlider(52)
        assertEquals(125.0, vm.state.value.tempF, 0.0)
    }

    @Test
    fun setTempFAndRhClamp() {
        val vm = ComfortViewModel()
        vm.setTempF(-10.0)
        assertEquals(0.0, vm.state.value.tempF, 0.0)
        vm.setTempF(200.0)
        assertEquals(125.0, vm.state.value.tempF, 0.0)
        vm.setRh(-5)
        assertEquals(0, vm.state.value.rh)
        vm.setRh(150)
        assertEquals(100, vm.state.value.rh)
    }

    @Test
    fun tabSwitch() {
        val vm = ComfortViewModel()
        vm.setTab(Tab.GAUGE)
        assertEquals(Tab.GAUGE, vm.state.value.tab)
    }
}
