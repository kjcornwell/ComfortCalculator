package com.root3.comfortgauge.data

import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.ui.Tab
import kotlinx.coroutines.flow.Flow

data class StoredSettings(val unit: TempUnit, val tab: Tab)

/** Persisted display preferences. The DataStore implementation lands in Phase 3. */
interface SettingsStore {
    val settings: Flow<StoredSettings>
    suspend fun setUnit(unit: TempUnit)
    suspend fun setTab(tab: Tab)
}
