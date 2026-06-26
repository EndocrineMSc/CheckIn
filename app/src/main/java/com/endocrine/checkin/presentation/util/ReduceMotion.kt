package com.endocrine.checkin.presentation.util

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * `true` when the user has disabled system animations (Settings → Developer options /
 * Accessibility "Remove animations"). Mirrors the web `prefers-reduced-motion` check so the
 * emotion wheel can skip its "bloom" animation. Always `false` in Compose previews.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    if (LocalInspectionMode.current) return false
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
