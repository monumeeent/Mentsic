/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.metrolist.music.R

/**
 * Mask choices for the profile picture (feature #64).
 *
 * "Blob" is approximated with an asymmetric rounded square: Compose has no built-in organic blob
 * path, and a custom [Shape] would need a Path per size. The four-corner radii still read as a
 * distinctly non-circular silhouette next to the others.
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
            // A squircle is a circle with softened sides; 30% rounding is the closest match
            // available without a custom path.
            SQUIRCLE -> RoundedCornerShape(30.dp)
            ROUNDED_SQUARE -> RoundedCornerShape(18.dp)
            // Cut corners give the flat-sided hexagon look.
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
 * The profile picture used in the app bar, the account sheet and the home header.
 *
 * Every surface goes through this composable so the user's chosen mask applies everywhere at
 * once, and so the fallback (no picture, or no account) is identical in all of them.
 */
@Composable
fun ProfileAvatar(
    imageUrl: String?,
    shape: AvatarShape,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val clipped = modifier.size(size).clip(shape.toShape())
    if (imageUrl.isNullOrBlank()) {
        Box(modifier = clipped, contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.person),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = clipped,
        )
    }
}
