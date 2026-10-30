package com.root3.comfortgauge.ui.map

import android.graphics.Bitmap
import android.util.Log
import com.root3.comfortgauge.model.ComfortModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Map extent in °F (10–38 °C); outside it is uniformly cold or hot. */
const val MAP_XMIN = 50.0
const val MAP_XMAX = 100.0

/**
 * Process-wide cache of the comfort-map color grid. It depends only on the model, never on the
 * slider position: a coarse 1°F × 2% grid paints first, then the 0.25°F × 0.5% grid replaces it.
 */
object MapBitmap {
    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap: StateFlow<Bitmap?> = _bitmap.asStateFlow()

    // Process-lifetime scope: the grid outlives any single Activity (rotation, recreation).
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile private var started = false

    fun ensureBuilt() {
        if (started) return
        started = true
        scope.launch {
            try {
                _bitmap.value = build(1.0, 2.0)
                _bitmap.value = build(0.25, 0.5)
            } catch (e: Exception) {
                started = false
                Log.e("MapBitmap", "Comfort map grid build failed; will retry on next composition", e)
            }
        }
    }

    /** Row 0 is RH 100% (top of the map), column 0 is [MAP_XMIN]. Rows are split across cores. */
    suspend fun build(stepT: Double, stepRh: Double): Bitmap = coroutineScope {
        val gw = ((MAP_XMAX - MAP_XMIN) / stepT).roundToInt() + 1
        val gh = (100 / stepRh).roundToInt() + 1
        val px = IntArray(gw * gh)
        val chunk = (gh + CHUNKS - 1) / CHUNKS
        (0 until gh step chunk).map { j0 ->
            async {
                for (j in j0 until minOf(j0 + chunk, gh)) {
                    val rh = 100 - j * stepRh
                    for (i in 0 until gw) {
                        px[j * gw + i] = ComfortModel.assess(MAP_XMIN + i * stepT, rh).color.toInt()
                    }
                }
            }
        }.awaitAll()
        Bitmap.createBitmap(px, gw, gh, Bitmap.Config.ARGB_8888)
    }

    private const val CHUNKS = 8
}
