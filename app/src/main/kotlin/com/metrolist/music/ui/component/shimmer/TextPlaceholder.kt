package com.metrolist.music.ui.component.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TextPlaceholder(
    modifier: Modifier = Modifier,
    height: Dp? = null,
    shape: RoundedCornerShape? = null,
) {
    Box(
        modifier = modifier
            .let { if (height != null) it.height(height) else it.fillMaxSize() }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .let { if (shape != null) it.clip(shape) else it },
    )
}
