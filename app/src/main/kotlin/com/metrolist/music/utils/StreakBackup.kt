/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context
import com.metrolist.music.constants.StreakCurrentKey
import com.metrolist.music.constants.StreakHistoryKey
import com.metrolist.music.constants.StreakLongestKey
import com.metrolist.music.constants.StreakTotalKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.File

/**
 * Second safety net for listening streaks.
 *
 * Android Auto Backup (already enabled via `allowBackup` + `backup_rules.xml`) restores DataStore
 * on a new device — but only when the user has Backup turned on. Some do not. This writes a tiny
 * JSON copy of the streak to app-external storage (`Android/data/<pkg>/files`), which survives an
 * uninstall, and re-imports it on first launch when the local streak is empty.
 *
 * Only the streak is mirrored here — four integers and a date list. Copying the whole database
 * would be far larger and duplicates what Auto Backup already does.
 */
object StreakBackup {
    private const val FILE_NAME = "streak_backup.json"

    private fun file(context: Context): File =
        File(context.getExternalFilesDir(null), FILE_NAME)

    /** Writes the current streak snapshot. Call after the streak advances. */
    suspend fun save(context: Context) =
        withContext(Dispatchers.IO) {
            runCatching {
                val target = file(context) ?: return@runCatching
                // Only back up the guest/primary streak; per-account streaks are covered by the
                // account itself and would need a growing map for little benefit.
                val json =
                    JSONObject().apply {
                        put("current", context.dataStore.get(StreakCurrentKey, 0))
                        put("longest", context.dataStore.get(StreakLongestKey, 0))
                        put("total", context.dataStore.get(StreakTotalKey, 0))
                        put("history", context.dataStore.get(StreakHistoryKey, ""))
                        put("savedAt", System.currentTimeMillis())
                    }
                target.writeText(json.toString())
            }.onFailure { Timber.w(it, "Streak backup write failed") }
        }

    /**
     * Restores the streak when the local copy is empty.
     *
     * Never overwrites real local data: a user who reinstalled and played a song already has a
     * (shorter) streak, and clobbering it with an older backup would be wrong.
     */
    suspend fun restoreIfEmpty(context: Context) =
        withContext(Dispatchers.IO) {
            runCatching {
                val current = context.dataStore.get(StreakCurrentKey, 0)
                val total = context.dataStore.get(StreakTotalKey, 0)
                if (current > 0 || total > 0) return@runCatching

                val source = file(context) ?: return@runCatching
                if (!source.exists()) return@runCatching

                val json = JSONObject(source.readText())
                context.safeDataStoreEdit { prefs ->
                    prefs[StreakCurrentKey] = json.optInt("current", 0)
                    prefs[StreakLongestKey] = json.optInt("longest", 0)
                    prefs[StreakTotalKey] = json.optInt("total", 0)
                    prefs[StreakHistoryKey] = json.optString("history", "")
                }
            }.onFailure { Timber.w(it, "Streak restore failed") }
        }
}
