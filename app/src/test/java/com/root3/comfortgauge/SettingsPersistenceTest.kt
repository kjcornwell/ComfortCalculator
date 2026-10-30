package com.root3.comfortgauge

import com.root3.comfortgauge.data.SettingsStore
import com.root3.comfortgauge.data.StoredSettings
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.ui.ComfortViewModel
import com.root3.comfortgauge.ui.Tab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeStore(initial: StoredSettings) : SettingsStore {
    val state = MutableStateFlow(initial)
    override val settings: StateFlow<StoredSettings> = state
    override suspend fun setUnit(unit: TempUnit) { state.value = state.value.copy(unit = unit) }
    override suspend fun setTab(tab: Tab) { state.value = state.value.copy(tab = tab) }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsPersistenceTest {

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun restoresSavedUnitAndTab() {
        val vm = ComfortViewModel(FakeStore(StoredSettings(TempUnit.C, Tab.GAUGE)))
        val s = vm.state.value
        assertTrue(s.loaded)
        assertEquals(TempUnit.C, s.unit)
        assertEquals(Tab.GAUGE, s.tab)
        assertEquals("24°C", s.tempLabel)
    }

    @Test
    fun choicesAreWrittenBack() {
        val store = FakeStore(StoredSettings(TempUnit.F, Tab.MAP))
        val vm = ComfortViewModel(store)
        vm.setUnit(TempUnit.C)
        vm.setTab(Tab.GAUGE)
        assertEquals(StoredSettings(TempUnit.C, Tab.GAUGE), store.state.value)
    }

    @Test
    fun withoutStoreStateIsLoadedImmediately() {
        assertTrue(ComfortViewModel().state.value.loaded)
    }
}
