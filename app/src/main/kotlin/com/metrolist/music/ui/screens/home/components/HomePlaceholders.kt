/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.music.constants.ListItemHeight
import com.metrolist.music.ui.component.shimmer.ListItemPlaceHolder
import com.metrolist.music.ui.component.shimmer.ShimmerHost
import com.metrolist.music.ui.component.shimmer.TextPlaceholder

/**
 * Shimmer placeholder composables for Home sections.
 *
 * Rules:
 * - All placeholders are wrapped in [ShimmerHost] at their call site — these functions
 *   assume they are inside an active shimmer scope.
 * - Sizes mirror production layout sizes exactly to prevent layout shift on load.
 * - No hardcoded colours — shimmer colouring is handled by [ShimmerHost].
 */

/** Full-width shimmer placeholder for the Speed Dial pager grid (3×3 tile layout). */
@Composable
fun SpeedDialPlaceholder(
    itemWidth: Dp,
    rows: Int,
    columns: Int,
    modifier: Modifier = Modifier,
) {
    ShimmerHost {
        Column(modifier = modifier.fillMaxWidth()) {
            repeat(rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(columns) {
                        Box(
                            modifier =
                                Modifier
                                    .width(itemWidth)
                                    .height(itemWidth)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(itemWidth - 8.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Shimmer for a 4-row Quick Picks / Forgotten Favorites horizontal grid. */
@Composable
fun SongGridPlaceholder(
    itemWidth: Dp,
    rowCount: Int = 4,
    columnCount: Int = 2,
    modifier: Modifier = Modifier,
) {
    ShimmerHost {
        Column(modifier = modifier.fillMaxWidth()) {
            repeat(rowCount) {
                repeat(columnCount) {
                    ListItemPlaceHolder()
                }
            }
        }
    }
}

/** Shimmer for a horizontal carousel row (albums, playlists, artists). */
@Composable
fun CarouselRowPlaceholder(
    cardSize: Dp = 160.dp,
    cardCount: Int = 4,
    modifier: Modifier = Modifier,
) {
    ShimmerHost {
        Row(modifier = modifier.padding(horizontal = 16.dp)) {
            repeat(cardCount) {
                Column(
                    modifier = Modifier.padding(end = 12.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(cardSize)
                                .clip(RoundedCornerShape(12.dp)),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextPlaceholder(
                        modifier = Modifier.width(cardSize * 0.75f),
                        height = 16.dp,
                        shape = RoundedCornerShape(4.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextPlaceholder(
                        modifier = Modifier.width(cardSize * 0.5f),
                        height = 12.dp,
                        shape = RoundedCornerShape(4.dp),
                    )
                }
            }
        }
    }
}

/** Section header shimmer (title bar skeleton). */
@Composable
fun SectionHeaderPlaceholder(modifier: Modifier = Modifier) {
    ShimmerHost {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextPlaceholder(
                modifier = Modifier.width(160.dp),
                height = 24.dp,
                shape = RoundedCornerShape(6.dp),
            )
        }
    }
}

