/**
 * Mentsic Material 3 Expressive connected button group.
 *
 * Medium buttons are 56dp tall and fully rounded (pill). A connected group is a row
 * with 3dp gaps where only the inner adjoining corners shrink to 8dp; the outer corners
 * stay round. Filled uses primary, tonal uses secondaryContainer, outlined has a 1dp
 * outlineVariant border.
 */

package com.metrolist.music.ui.component.mentsic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.Shape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.TextStyle

@Stable
enum class MentsicButtonType { Filled, Tonal, Outlined }

@Stable
data class MentsicButtonItem(
    val label: String,
    val icon: ImageVector? = null,
    val type: MentsicButtonType = MentsicButtonType.Tonal,
    val onClick: () -> Unit = {},
)

@Stable
data class MentsicButtonSize(
    val height: Dp,
    val corner: Dp,
    val labelSize: androidx.compose.material3.TextStyle,
    val iconSize: Dp,
    val horizontalPadding: Dp,
) {
    companion object {
        val ExtraSmall = MentsicButtonSize(32.dp, 16.dp, MaterialTheme.typography.labelSmall, 18.dp, 8.dp)
        val Small = MentsicButtonSize(40.dp, 20.dp, MaterialTheme.typography.labelMedium, 20.dp, 10.dp)
        val Medium = MentsicButtonSize(56.dp, 28.dp, MaterialTheme.typography.labelLarge, 24.dp, 16.dp)
        val Large = MentsicButtonSize(96.dp, 48.dp, MaterialTheme.typography.titleLarge, 32.dp, 24.dp)
        val ExtraLarge = MentsicButtonSize(136.dp, 68.dp, MaterialTheme.typography.headlineSmall, 40.dp, 32.dp)
    }
}

@Composable
fun RowScope.MentsicButton(
    item: MentsicButtonItem,
    size: MentsicButtonSize,
    shape: androidx.compose.foundation.shape.Shape,
    modifier: Modifier = Modifier,
) {
    val contentColor = LocalContentColor.current
    val backgroundColor: Color
    val borderColor: Color?
    val labelColor: Color
    val iconColor: Color

    when (item.type) {
        MentsicButtonType.Filled -> {
            backgroundColor = MaterialTheme.colorScheme.primary
            borderColor = null
            labelColor = MaterialTheme.colorScheme.onPrimary
            iconColor = MaterialTheme.colorScheme.onPrimary
        }
        MentsicButtonType.Tonal -> {
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
            borderColor = null
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer
        }
        MentsicButtonType.Outlined -> {
            backgroundColor = Color.Transparent
            borderColor = MaterialTheme.colorScheme.outlineVariant
            labelColor = contentColor
            iconColor = contentColor
        }
    }

    Box(
        modifier = modifier
            .height(size.height)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = if (item.type == MentsicButtonType.Outlined) 1.dp else 0.dp,
                color = borderColor ?: Color.Transparent,
                shape = shape,
            )
            .clickable(
                onClick = item.onClick,
                role = androidx.compose.foundation.role.Role.Button,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = size.horizontalPadding),
        ) {
            item.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(size.iconSize),
                    tint = iconColor,
                )
                androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
            }
            Text(
                text = item.label,
                style = size.labelSize,
                color = labelColor,
            )
        }
    }
}

@Composable
fun ConnectedButtonGroup(
    items: List<MentsicButtonItem>,
    size: MentsicButtonSize = MentsicButtonSize.Medium,
    onButtonClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val count = items.size
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        items.forEachIndexed { index, item ->
            val isFirst = index == 0
            val isLast = index == count - 1
            val half = size.corner
            val shape = when {
                count == 1 -> RoundedCornerShape(half)
                isFirst -> RoundedCornerShape(half, 8.dp, half, 8.dp)
                isLast -> RoundedCornerShape(8.dp, half, 8.dp, half)
                else -> RoundedCornerShape(8.dp)
            }
            item(key = "button_$index") {
                MentsicButton(
                    item = item.copy(onClick = { onButtonClick(index) }),
                    size = size,
                    shape = shape,
                )
            }
        }
    }
}