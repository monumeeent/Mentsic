/**
 * Mentsic Material 3 Expressive navigation bar.
 *
 * 80dp tall on surfaceContainer, with the background extended down through the gesture
 * navigation area (the caller pads the bottom by the system inset). The active
 * destination shows a secondaryContainer pill indicator (64×32dp), a filled icon and a
 * labelMedium label.
 */

package com.metrolist.music.ui.component.mentsic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.metrolist.music.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun MentsicNavigationBar(
    selectedIndex: Int,
    onDestinationClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val destinations = listOf(
        MentsicNavDestination(R.string.home, R.drawable.home_outlined, R.drawable.home_filled),
        MentsicNavDestination(R.string.explore, R.drawable.explore_outlined, R.drawable.explore_filled),
        MentsicNavDestination(R.string.filter_library, R.drawable.library_music_outlined, R.drawable.library_music_filled),
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth().height(80.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            destinations.forEachIndexed { index, destination ->
                val isSelected = index == selectedIndex
                val iconRes = if (isSelected) destination.iconActive else destination.iconInactive

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDestinationClick(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(64.dp, 32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                        )
                    }

                    Column(
                        modifier = Modifier.padding(bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        Text(
                            text = stringResource(destination.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            },
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

private data class MentsicNavDestination(
    val labelRes: Int,
    val iconInactive: Int,
    val iconActive: Int,
)