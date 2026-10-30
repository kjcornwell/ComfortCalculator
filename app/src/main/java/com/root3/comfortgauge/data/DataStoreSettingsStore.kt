package com.root3.comfortgauge.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.ui.Tab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Persists the display unit and the hero tab (prototype: `cg-unit`, `cg-tab`). */
class DataStoreSettingsStore(private val store: DataStore<Preferences>) : SettingsStore {

    override val settings: Flow<StoredSettings> = store.data
        .catch { e ->
            if (e is IOException) {
                Log.e(TAG, "Reading settings failed; falling back to defaults", e)
                emit(emptyPreferences())
            } else {
                throw e
            }
        }
        .map { p ->
            StoredSettings(
                unit = p[UNIT]?.let { name -> TempUnit.entries.firstOrNull { it.name == name } } ?: TempUnit.F,
                tab = p[TAB]?.let { name -> Tab.entries.firstOrNull { it.name == name } } ?: Tab.MAP,
            )
        }

    override suspend fun setUnit(unit: TempUnit) {
        store.edit { it[UNIT] = unit.name }
    }

    override suspend fun setTab(tab: Tab) {
        store.edit { it[TAB] = tab.name }
    }

    private companion object {
        const val TAG = "SettingsStore"
        val UNIT = stringPreferencesKey("unit")
        val TAB = stringPreferencesKey("tab")
    }
}
