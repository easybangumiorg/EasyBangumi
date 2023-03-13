package com.heyanle.easybangumi4.ui.common.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.ui.common.player.utils.TimeUtils

/**
 * Created by HeYanLe on 2023/3/8 22:04.
 * https://github.com/heyanLE
 */

@Composable
fun TopControl(
    content: @Composable RowScope.() -> Unit,
){
    Row(
        modifier = Modifier.fillMaxWidth().background(
            brush = Brush.verticalGradient(
                listOf(Color.Black, Color.Transparent),
            )
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
        content = content
    )
}

@Composable
fun BottomControl(
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(
            brush = Brush.verticalGradient(
                listOf( Color.Transparent, Color.Black)
            )
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        content = content
    )
}

/**
 * 播放按钮
 */
@Composable
fun RowScope.PlayPauseBtn(
    isPlaying: Boolean,
    onClick: (Boolean) -> Unit,
) {
    IconButton(onClick = {
        onClick(!isPlaying)
    }) {
        val icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow
        Icon(
            icon,
            tint = Color.White,
            contentDescription = if (isPlaying) stringResource(id = com.heyanle.easy_i18n.R.string.pause) else stringResource(
                id = com.heyanle.easy_i18n.R.string.play
            )
        )
    }
}

/**
 * 时间字符串 00:00
 */
@Composable
fun RowScope.TimeText(
    time: Long,
    color: Color = Color.Unspecified,
){
    Text(text = TimeUtils.toString(time), color = color)
}


@Composable
fun RowScope.TimeSlider(
    during: Long,
    position: Long,
    onValueChange: (Long) -> Unit,
    onValueChangeFinish: ()->Unit,
){
    Slider(
        modifier = Modifier.weight(1f),
        value = position.toFloat(),
        onValueChange = {
            onValueChange(it.toLong())
        },
        onValueChangeFinished = onValueChangeFinish,
        valueRange = 0F .. during.toFloat().coerceAtLeast(0F)
    )
}

@Composable
fun RowScope.FullScreenBtn(
    isFullScreen:  Boolean,
    onClick: (Boolean) -> Unit,
) {
    IconButton(onClick = {
        onClick(!isFullScreen)
    }) {
        val icon = if (isFullScreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen
        Icon(
            icon,
            tint = Color.White,
            contentDescription = if (isFullScreen) stringResource(id = com.heyanle.easy_i18n.R.string.full_screen_exit) else stringResource(
                id = com.heyanle.easy_i18n.R.string.full_screen
            )
        )
    }
}

@Composable
fun RowScope.BackBtn(
    onClick: () -> Unit,
){
    IconButton(onClick = {
        onClick()
    }) {
        Icon(
            Icons.Filled.ArrowBack,
            tint = Color.White,
            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.back)
        )
    }
}





