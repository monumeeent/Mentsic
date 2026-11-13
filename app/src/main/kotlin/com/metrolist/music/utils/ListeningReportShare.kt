/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.metrolist.music.R
import com.metrolist.music.models.ListeningReport
import java.io.File
import java.io.FileOutputStream

/**
 * Renders the recap card to a PNG and hands it to the system share sheet.
 *
 * The image is drawn with plain [Canvas] rather than Compose screenshotting so it can be produced
 * off the main thread and without a window: a Compose capture would need to be laid out first and
 * would snapshot whatever the user is currently looking at.
 */
object ListeningReportShare {
    private const val WIDTH = 1080
    private const val HEIGHT = 1440
    private const val TAG = "listening_report"

    /** Draws [report] to a bitmap, writes it into the app cache and returns the share intent. */
    fun buildShareIntent(context: Context, report: ListeningReport): Intent? {
        val bitmap = render(context, report) ?: return null
        val file = writeToCache(context, bitmap) ?: return null
        val uri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.FileProvider",
                file,
            )
        val caption =
            context.getString(
                R.string.report_share_text,
                report.periodLabel,
                report.totalMinutes,
            )
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun render(context: Context, report: ListeningReport): Bitmap? =
        runCatching {
            val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background: brand gradient from the theme seed so the card matches the app.
            val background = Paint()
            background.shader =
                LinearGradient(
                    0f,
                    0f,
                    WIDTH.toFloat(),
                    HEIGHT.toFloat(),
                    intArrayOf(Color.parseColor("#1B1035"), Color.parseColor("#0B0B0F"), Color.parseColor("#1D0F2E")),
                    null,
                    Shader.TileMode.CLAMP,
                )
            canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), background)

            val titlePaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 64f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
            // Long titles are statically fitted: ellipsizing a Canvas draw needs a text layout.
            fun fitted(text: String, paint: Paint, maxWidth: Float): String {
                if (paint.measureText(text) <= maxWidth) return text
                var end = text.length
                while (end > 0 && paint.measureText(text.take(end) + "…") > maxWidth) end--
                return text.take(end) + "…"
            }
            val labelPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#C9C0E8")
                    textSize = 38f
                }
            val valuePaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 92f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
            val rowPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#EDEAF7")
                    textSize = 40f
                }
            val accentPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#B79CFF")
                    textSize = 34f
                }

            var y = 140f
            canvas.drawText(
                fitted(context.getString(R.string.report_share_title), titlePaint, WIDTH - 144f),
                72f,
                y,
                titlePaint,
            )
            y += 62f
            canvas.drawText(report.periodLabel, 72f, y, labelPaint)

            // Stat band: minutes, plays, artists.
            y += 150f
            val stats =
                listOf(
                    report.totalMinutes.toString() to context.getString(R.string.report_minutes),
                    report.totalPlays.toString() to context.getString(R.string.report_plays),
                    report.uniqueArtists.toString() to context.getString(R.string.report_artists),
                )
            stats.forEachIndexed { index, (value, label) ->
                val x = 72f + index * 320f
                canvas.drawText(value, x, y, valuePaint)
                canvas.drawText(label, x, y + 48f, labelPaint)
            }

            // Divider
            y += 100f
            val divider = Paint().apply { color = Color.parseColor("#33FFFFFF") }
            canvas.drawRect(72f, y, WIDTH - 72f, y + 2f, divider)

            // Top tracks
            y += 78f
            canvas.drawText(context.getString(R.string.report_top_tracks), 72f, y, accentPaint)
            y += 66f
            report.topSongs.take(5).forEachIndexed { index, highlight ->
                canvas.drawText("${index + 1}.", 72f, y, labelPaint)
                canvas.drawText(
                    fitted(highlight.title, rowPaint, WIDTH - 128f - 72f - 90f),
                    128f,
                    y,
                    rowPaint,
                )
                val count = "${highlight.playCount}×"
                canvas.drawText(count, WIDTH - 72f - rowPaint.measureText(count), y, accentPaint)
                y += 58f
            }

            // Top artists, wrapped into two lines.
            y += 46f
            canvas.drawText(context.getString(R.string.report_top_artists), 72f, y, accentPaint)
            y += 60f
            report.topArtistNames.take(4).chunked(2).forEach { line ->
                canvas.drawText(
                    fitted(line.joinToString(" · "), rowPaint, WIDTH - 144f),
                    72f,
                    y,
                    rowPaint,
                )
                y += 56f
            }

            // Footer badge
            val badge = RectF(72f, HEIGHT - 170f, 72f + 360f, HEIGHT - 100f)
            val badgePaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#22FFFFFF")
                }
            canvas.drawRoundRect(badge, 36f, 36f, badgePaint)
            canvas.drawText(context.getString(R.string.report_badge), 104f, HEIGHT - 120f, accentPaint)

            bitmap
        }.getOrNull()

    private fun writeToCache(context: Context, bitmap: Bitmap): File? =
        runCatching {
            val dir = File(context.cacheDir, TAG).apply { mkdirs() }
            // Single reusable filename: the FileProvider path is already scoped to this folder.
            val file = File(dir, "recap.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file
        }.getOrNull()
}
