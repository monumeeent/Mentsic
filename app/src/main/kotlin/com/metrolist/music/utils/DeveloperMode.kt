/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

/**
 * Typing this exact string in the search bar toggles developer options.
 *
 * Kept as a code constant rather than a string resource so it cannot be discovered by
 * unpacking the APK's resources.
 */
const val DEVELOPER_UNLOCK_CODE = "@#monu"

/** True when [text] is the developer unlock code (surrounding whitespace ignored). */
fun isDeveloperUnlockCode(text: String): Boolean = text.trim() == DEVELOPER_UNLOCK_CODE
