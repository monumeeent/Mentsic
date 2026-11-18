/**
 * Mentsic Music — launcher icon variant switching.
 *
 * The manifest declares three launcher components:
 *
 *  - `MainActivity`        — themed adaptive icon (`@mipmap/ic_launcher`, Material You).
 *  - `MainActivityAlias`   — legacy upgrade shim, same themed icon.
 *  - `MainActivityStatic`  — the real Mentsic logo (`@mipmap/ic_launcher_static`).
 *
 * Exactly one must be enabled at a time, otherwise the launcher picks one of the duplicates
 * non-deterministically — which is why the icon appeared to "follow" the themed icon even after
 * the static one was added. [applyLauncherIconVariant] makes that choice explicit and persists it
 * in DataStore so the next cold start keeps the same icon.
 *
 * Names are resolved with [Context.getPackageName] rather than a hardcoded package: release builds
 * may override the application id via `METROLIST_APPLICATION_ID`.
 */

package com.metrolist.music.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber

/** Launcher alias class names — kept in sync with `AndroidManifest.xml`. */
private const val LEGACY_ALIAS_CLASS = "com.metrolist.music.MainActivityAlias"
private const val STATIC_ALIAS_CLASS = "com.metrolist.music.MainActivityStatic"

const val LAUNCHER_ICON_DYNAMIC = "dynamic"
const val LAUNCHER_ICON_STATIC = "static"

/**
 * Applies [variant] (`dynamic` or `static`).
 *
 * Component changes can kill the process, so call this from a background dispatcher.
 */
fun applyLauncherIconVariant(
    context: Context,
    variant: String,
) {
    val packageName = context.packageName
    val staticEnabled = variant == LAUNCHER_ICON_STATIC
    val themedState =
        if (staticEnabled) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

    try {
        val packageManager = context.packageManager
        // The main activity is never toggled: it is the target of both aliases and of every
        // deep link, so disabling it would break the app. Only the *aliases* decide which icon
        // the launcher shows.
        val themedComponents =
            listOf(LEGACY_ALIAS_CLASS).map { ComponentName(packageName, it) }

        themedComponents.forEach { component ->
            packageManager.setComponentEnabledSetting(
                component,
                themedState,
                PackageManager.DONT_KILL_APP,
            )
        }

        packageManager.setComponentEnabledSetting(
            ComponentName(packageName, STATIC_ALIAS_CLASS),
            if (staticEnabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            PackageManager.DONT_KILL_APP,
        )
    } catch (e: Exception) {
        // Some OEM launchers refuse component changes; the app keeps the previously applied icon.
        Timber.w(e, "Failed to apply launcher icon variant: $variant")
    }
}
