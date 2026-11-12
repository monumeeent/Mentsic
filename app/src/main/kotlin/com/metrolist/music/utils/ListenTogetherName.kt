/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import kotlin.random.Random

/** Prefix every Listen Together display name carries. */
const val LISTEN_TOGETHER_PREFIX = "ment."

private const val RANDOM_SUFFIX_LENGTH = 4
private const val RANDOM_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789"

/**
 * Builds the Listen Together display name.
 *
 * - With a saved username: `ment.<username>`
 * - Without one: `ment.<4 random chars>`
 *
 * An already-prefixed name is returned unchanged, so re-joining never produces `ment.ment.x`.
 */
fun listenTogetherName(savedUsername: String?): String {
    val base = savedUsername?.trim().orEmpty()
    if (base.startsWith(LISTEN_TOGETHER_PREFIX)) return base
    val suffix = base.ifBlank { randomSuffix() }
    return LISTEN_TOGETHER_PREFIX + suffix
}

private fun randomSuffix(): String =
    buildString(RANDOM_SUFFIX_LENGTH) {
        repeat(RANDOM_SUFFIX_LENGTH) {
            append(RANDOM_ALPHABET[Random.nextInt(RANDOM_ALPHABET.length)])
        }
    }
