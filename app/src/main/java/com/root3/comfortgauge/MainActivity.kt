package com.root3.comfortgauge

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.root3.comfortgauge.data.DataStoreSettingsStore
import com.root3.comfortgauge.data.settingsDataStore
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.ui.ComfortViewModel
import com.root3.comfortgauge.ui.MainActions
import com.root3.comfortgauge.ui.MainScreen
import com.root3.comfortgauge.ui.Tab
import com.root3.comfortgauge.ui.installSplash
import com.root3.comfortgauge.ui.info.InfoScreen
import com.root3.comfortgauge.ui.map.MapBitmap
import com.root3.comfortgauge.ui.theme.Bg
import com.root3.comfortgauge.ui.theme.ComfortTheme

class MainActivity : ComponentActivity() {

    private val vm: ComfortViewModel by viewModels {
        ComfortViewModel.factory(DataStoreSettingsStore(applicationContext.settingsDataStore))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplash(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        MapBitmap.ensureBuilt()
        val debugExtras = savedInstanceState == null && isDebuggable()
        setContent {
            ComfortTheme {
                val state by vm.state.collectAsStateWithLifecycle()
                var showInfo by rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(state.loaded) {
                    if (state.loaded && debugExtras && applyDebugExtras(intent)) showInfo = true
                }
                Box(Modifier.fillMaxSize().background(Bg)) {
                    if (state.loaded) {
                        MainScreen(
                            state,
                            MainActions(
                                onTempSlider = vm::setTempFromSlider,
                                onRh = vm::setRh,
                                onMapPick = { f, rh -> vm.setTempF(f); vm.setRh(rh) },
                                onUnit = vm::setUnit,
                                onTab = vm::setTab,
                                onInfo = { showInfo = true },
                            ),
                            Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                        )
                    }
                    AnimatedVisibility(
                        visible = showInfo,
                        enter = fadeIn() + slideInVertically { it / 12 },
                        exit = fadeOut() + slideOutVertically { it / 12 },
                    ) {
                        InfoScreen(state.unit, onDone = { showInfo = false })
                    }
                }
                BackHandler(enabled = showInfo) { showInfo = false }
            }
        }
    }

    private fun isDebuggable() = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    /**
     * Debug builds only: `adb shell am start -S -n com.root3.comfortgauge/.MainActivity
     * --ef tempF 65 --ei rh 0 --es unit C --es tab gauge --ez info true` for screenshots.
     * Returns whether the info screen was requested.
     */
    private fun applyDebugExtras(i: Intent): Boolean {
        if (i.hasExtra("unit")) {
            TempUnit.entries.firstOrNull { it.name.equals(i.getStringExtra("unit"), true) }?.let(vm::setUnit)
        }
        if (i.hasExtra("tab")) {
            Tab.entries.firstOrNull { it.name.equals(i.getStringExtra("tab"), true) }?.let(vm::setTab)
        }
        if (i.hasExtra("tempF")) vm.setTempF(i.getFloatExtra("tempF", 75f).toDouble())
        if (i.hasExtra("rh")) vm.setRh(i.getIntExtra("rh", 50))
        return i.getBooleanExtra("info", false)
    }
}
