package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Compact, non-modal player feedback with an immediate undo/cancel action. */
@Composable
internal fun PlayerActionNotice(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color.Black.copy(alpha = 0.78f),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.widthIn(max = 420.dp).testTag("player-action-notice")
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(message, modifier = Modifier.weight(1f, fill = false), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            TextButton(onClick = onAction, modifier = Modifier.heightIn(min = 44.dp)) {
                Text(actionLabel, color = Color(0xFFFFD5DD), fontSize = 12.sp)
            }
        }
    }
}
