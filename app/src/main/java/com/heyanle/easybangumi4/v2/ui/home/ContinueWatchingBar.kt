package com.heyanle.easybangumi4.v2.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.cartoon_play.playerNoticeTime
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens

@Composable
internal fun ContinueWatchingBar(item: CartoonInfo, onResume: () -> Unit, onDismiss: () -> Unit) {
    Surface(color = V2Tokens.SurfaceMuted, modifier = Modifier.fillMaxWidth().testTag("continue-watching")) {
        Column {
            HorizontalDivider(color = V2Tokens.Divider)
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onResume).padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OkImage(
                    image = item.coverUrl,
                    contentDescription = "${item.name}封面",
                    errorColor = V2Tokens.Divider,
                    modifier = Modifier.size(width = 44.dp, height = 54.dp).clip(RoundedCornerShape(6.dp)),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, color = V2Tokens.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${item.lastEpisodeLabel.takeIf { it.isNotBlank() }?.let { "继续$it" } ?: "继续观看"} · ${playerNoticeTime(item.lastProcessTime)}",
                        color = V2Tokens.TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                IconButton(
                    onClick = onResume,
                    modifier = Modifier.size(44.dp).background(V2Theme.colors.accentContainer, CircleShape).testTag("continue-watching-play"),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "继续观看${item.name}", tint = V2Theme.colors.accent)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp).testTag("continue-watching-close")) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭继续观看", tint = V2Tokens.TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
            if (item.lastTotalTile > 0) {
                LinearProgressIndicator(
                    progress = { (item.lastProcessTime.toFloat() / item.lastTotalTile).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = V2Theme.colors.accent,
                    trackColor = V2Tokens.Divider,
                    drawStopIndicator = {},
                )
            }
        }
    }
}
