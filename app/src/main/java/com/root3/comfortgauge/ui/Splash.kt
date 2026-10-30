package com.root3.comfortgauge.ui

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import com.root3.comfortgauge.R

private const val HOLD_MS = 650L
private const val EXIT_MS = 280L

/**
 * Call before super.onCreate(). Swaps the launch theme for the app theme and, on Android 12+,
 * holds the system splash just long enough for the animated icon to finish, then fades it out.
 */
fun ComponentActivity.installSplash(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_ComfortGauge)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || savedInstanceState != null) return

    val start = SystemClock.uptimeMillis()
    val content = findViewById<View>(android.R.id.content)
    content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            val ready = SystemClock.uptimeMillis() - start >= HOLD_MS
            if (ready) content.viewTreeObserver.removeOnPreDrawListener(this)
            return ready
        }
    })

    splashScreen.setOnExitAnimationListener { splash ->
        splash.iconView?.animate()
            ?.scaleX(1.12f)?.scaleY(1.12f)
            ?.setDuration(EXIT_MS)
            ?.setInterpolator(AccelerateInterpolator())
            ?.start()
        splash.animate()
            .alpha(0f)
            .setDuration(EXIT_MS)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction { splash.remove() }
            .start()
    }
}
