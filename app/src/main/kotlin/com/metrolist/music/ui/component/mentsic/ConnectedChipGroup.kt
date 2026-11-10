/**
 * Mentsic Material 3 Expressive chip components.
 *
 * Chips are 32dp tall by default (XS) with 8dp corners. A connected chip group is a
 * horizontally-scrolling row with 3dp gaps where only the inner adjoining corners shrink
 * to 4dp; the outer corners stay round and every chip shares one height.
 */

package com.metrolist.music.ui.component.mentsic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Stable
data class MentsicChipItem(
    val label: String,
    val selected: Boolean = false,
    val onClick: () -> Unit = {},
)

@Composable
fun MentsicChip(
    item: MentsicChipItem,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val contentColor = LocalContentColor.current
    val isSelected = item.selected
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val labelColor = if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        contentColor
    }

    Box(
        modifier = modifier
            .height(32.dp)
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                onClick = item.onClick,
                role = Role.Button,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                color = labelColor,
            )
        }
    }
}

@Composable
fun ConnectedChipGroup(
    items: List<MentsicChipItem>,
    selectedIndex: Int,
    onChipClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chipCount = items.size
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        itemsIndexed(items, key = { index, _ -> "chip_$index" }) { index, item ->
            val isFirst = index == 0
            val isLast = index == chipCount - 1
            val shape = when {
                chipCount == 1 -> RoundedCornerShape(8.dp)
                isFirst -> RoundedCornerShape(8.dp, 4.dp, 8.dp, 4.dp)
                isLast -> RoundedCornerShape(4.dp, 8.dp, 4.dp, 8.dp)
                else -> RoundedCornerShape(4.dp)
            }
            MentsicChip(
                item = item.copy(
                    selected = index == selectedIndex,
                    onClick = { onChipClick(index) },
                ),
                shape = shape,
            )
        }
    }
}