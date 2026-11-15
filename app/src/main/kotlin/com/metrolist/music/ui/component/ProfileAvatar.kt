/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.metrolist.music.R

/**
 * Mask choices for the profile picture (feature #64).
 *
 * "Blob" is an asymmetric rounded square: Compose has no built-in organic blob path, and a custom
 * [Shape] would need a per-size Path. The four differing radii still read as a distinctly
 * non-circular silhouette next to the others.
 */
enum class AvatarShape(
    val key: String,
) {
    CIRCLE("circle"),
    SQUIRCLE("squircle"),
    ROUNDED_SQUARE("rounded_square"),
    HEXAGON("hexagon"),
    BLOB("blob"),
    ;

    fun toShape(): Shape =
        when (this) {
            CIRCLE -> CircleShape
            SQUIRCLE -> RoundedCornerShape(30.dp)
            ROUNDED_SQUARE -> RoundedCornerShape(18.dp)
            HEXAGON -> CutCornerShape(28.dp)
            BLOB ->
                RoundedCornerShape(
                    topStart = 55.dp,
                    topEnd = 30.dp,
                    bottomEnd = 55.dp,
                    bottomStart = 30.dp,
                )
        }

    /** Localised name shown in the shape picker. */
    @Composable
    fun label(): String =
        stringResource(
            when (this) {
                CIRCLE -> R.string.avatar_shape_circle
                SQUIRCLE -> R.string.avatar_shape_squircle
                ROUNDED_SQUARE -> R.string.avatar_shape_rounded
                HEXAGON -> R.string.avatar_shape_hexagon
                BLOB -> R.string.avatar_shape_blob
            },
        )

    companion object {
        val Default = CIRCLE

        fun fromKey(key: String?): AvatarShape = entries.firstOrNull { it.key == key } ?: Default
    }
}

/**
 * A built-in avatar. No upload, no gallery permission: the user picks one of these, and it is
 * rendered as a tonal gradient with a glyph, so it survives reinstall and works offline.
 *
 * @param key stable identifier persisted in DataStore.
 * @param glyph monogram drawn on top (single character, or empty for a pure gradient).
 * @param base base colour of the gradient.
 */
enum class AvatarPreset(
    val key: String,
    val glyph: String,
    val base: Color,
) {
    AMBER("amber", "A", Color(0xFFF2A93B)),
    CORAL("coral", "C", Color(0xFFE8615A)),
    ROSE("rose", "R", Color(0xFFD46A8C)),
    VIOLET("violet", "V", Color(0xFF8B5CF6)),
    INDIGO("indigo", "I", Color(0xFF5B6BE0)),
    SKY("sky", "S", Color(0xFF3AA0E8)),
    TEAL("teal", "T", Color(0xFF2E9E8F)),
    MINT("mint", "M", Color(0xFF4CB782)),
    SAND("sand", "N", Color(0xFFC9A227)),
    SLATE("slate", "L", Color(0xFF5A6B7C)),
    ;

    /** Two-stop gradient derived from [base], so every preset reads as a distinct swatch. */
    fun gradient(): Brush =
        Brush.linearGradient(
            listOf(
                base,
                base.copy(red = base.red * 0.75f, green = base.green * 0.75f, blue = base.blue * 0.85f),
            ),
        )

    companion object {
        fun fromKey(key: String?): AvatarPreset? = entries.firstOrNull { it.key == key }
    }
}

/**
 * The single profile-picture entry point used by the app bar, the account sheet and the picker.
 *
 * Resolution order: chosen preset → YouTube account picture → generic silhouette. Every surface
 * routes through here so the chosen mask and preset apply everywhere at once.
 */
@Composable
fun ProfileAvatar(
    presetKey: String?,
    accountImageUrl: String?,
    shape: AvatarShape,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val clipped = modifier.size(size).clip(shape.toShape())
    val preset = AvatarPreset.fromKey(presetKey)

    when {
        preset != null ->
            Box(modifier = clipped.background(preset.gradient()), contentAlignment = Alignment.Center) {
                Text(
                    text = preset.glyph,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    // Always light glyphs: every preset hue is dark enough for white to read.
                    color = Color.White,
                )
            }

        !accountImageUrl.isNullOrBlank() ->
            AsyncImage(
                model = accountImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = clipped,
            )

        else ->
            Box(
                modifier = clipped.background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.person),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
    }
}
