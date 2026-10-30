package com.root3.comfortgauge.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.root3.comfortgauge.data.SettingsStore
import com.root3.comfortgauge.model.ComfortModel
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.model.Units
import com.root3.comfortgauge.model.Verdict
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Tab { MAP, GAUGE }

/** [tempF] is the exact source of truth; the slider is a view of it in the display unit. */
data class UiState(
    val tempF: Double = 75.0,
    val rh: Int = 50,
    val unit: TempUnit = TempUnit.F,
    val tab: Tab = Tab.MAP,
    /** False until persisted unit/tab have been restored, so the first frame never flips. */
    val loaded: Boolean = true,
) {
    val verdict: Verdict = ComfortModel.verdict(tempF, rh.toDouble())
    val sliderRange: IntRange = if (unit == TempUnit.C) C_RANGE else F_RANGE
    val sliderValue: Int = Units.jsRound(Units.display(tempF, unit))
    val tempLabel: String = Units.format(tempF, unit)

    companion object {
        const val MIN_F = 0.0
        const val MAX_F = 125.0
        val F_RANGE = 0..125
        val C_RANGE = -18..52
    }
}

class ComfortViewModel(private val store: SettingsStore? = null) : ViewModel() {

    private val _state = MutableStateFlow(UiState(loaded = store == null))
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        store?.let { s ->
            viewModelScope.launch {
                val saved = try {
                    s.settings.first()
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("ComfortViewModel", "Restoring unit/tab failed; using defaults", e)
                    null
                }
                _state.update {
                    if (saved == null) it.copy(loaded = true)
                    else it.copy(unit = saved.unit, tab = saved.tab, loaded = true)
                }
            }
        }
    }

    fun setTempF(f: Double) {
        _state.update { it.copy(tempF = f.coerceIn(UiState.MIN_F, UiState.MAX_F)) }
    }

    /** Slider reports whole degrees in the display unit. */
    fun setTempFromSlider(displayValue: Int) {
        val unit = _state.value.unit
        val f = if (unit == TempUnit.C) Units.c2f(displayValue.toDouble()) else displayValue.toDouble()
        setTempF(f)
    }

    fun setRh(rh: Int) {
        _state.update { it.copy(rh = rh.coerceIn(0, 100)) }
    }

    fun setUnit(unit: TempUnit) {
        _state.update { it.copy(unit = unit) }
        store?.let { s -> viewModelScope.launch { persist("unit") { s.setUnit(unit) } } }
    }

    fun setTab(tab: Tab) {
        _state.update { it.copy(tab = tab) }
        store?.let { s -> viewModelScope.launch { persist("tab") { s.setTab(tab) } } }
    }

    private suspend fun persist(what: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("ComfortViewModel", "Saving $what failed; the choice won't survive a restart", e)
        }
    }

    companion object {
        fun factory(store: SettingsStore): ViewModelProvider.Factory = viewModelFactory {
            initializer { ComfortViewModel(store) }
        }
    }
}
