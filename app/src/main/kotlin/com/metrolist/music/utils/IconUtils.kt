package com.metrolist.music.utils

import android.content.Context

/**
 * Launcher-icon switching.
 *
 * Deliberately a no-op. The dynamic (monochrome/themed) icon alias caused the launcher to show an
 * icon that did not match the app's own branding and could not be previewed before applying, so
 * the feature is retired: Mentsic always uses its static launcher icon.
 *
 * Kept as a type so existing call sites compile; the corresponding toggle is no longer shown.
 */
object IconUtils {
    @Suppress("UNUSED_PARAMETER")
    fun setIcon(context: Context, enabled: Boolean) {
        // Intentionally empty — see the class doc.
    }
}
