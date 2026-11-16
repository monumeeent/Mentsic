/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.recognition

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.metrolist.music.R

/**
 * Shazam-style floating recogniser.
 *
 * Shows a small capsule above other apps while a widget/notification-initiated recognition runs,
 * so the user does not lose their place. It is deliberately dumb: [RecognitionForegroundService]
 * owns the state machine and drives this overlay through [show]/[setState]/[hide].
 *
 * Requires `SYSTEM_ALERT_WINDOW`. Callers must check [canDraw] first; the service falls back to
 * the in-app sheet when the user has not granted it.
 */
object RecognitionOverlay {
    private var windowManager: WindowManager? = null
    private var rootView: View? = null
    private var params: WindowManager.LayoutParams? = null

    /** True when the user has granted "display over other apps". */
    fun canDraw(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    /** Deep-links to the system toggle; there is no in-app way to grant this. */
    fun requestPermission(context: Context) {
        runCatching {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    fun show(
        context: Context,
        title: String,
        subtitle: String,
    ) {
        if (!canDraw(context)) return

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val view = LayoutInflater.from(context).inflate(R.layout.recognition_overlay, null)
        (view.findViewById<TextView>(R.id.overlay_title)).text = title
        (view.findViewById<TextView>(R.id.overlay_subtitle)).text = subtitle
        view.setOnClickListener { hide() }

        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

        val lp =
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                // Not focusable: taps outside still reach the app underneath.
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                y = 160
            }

        params = lp
        runCatching {
            wm.addView(view, lp)
            rootView = view
        }
    }

    /** Updates the two lines without rebuilding the window. */
    fun setState(title: String, subtitle: String, isSuccess: Boolean = false) {
        val view = rootView ?: return
        view.findViewById<TextView>(R.id.overlay_title)?.text = title
        view.findViewById<TextView>(R.id.overlay_subtitle)?.text = subtitle
        view.findViewById<ImageView>(R.id.overlay_icon)?.alpha = if (isSuccess) 1f else 0.8f
    }

    fun hide() {
        val view = rootView ?: return
        runCatching { windowManager?.removeView(view) }
        rootView = null
        params = null
        windowManager = null
    }
}
