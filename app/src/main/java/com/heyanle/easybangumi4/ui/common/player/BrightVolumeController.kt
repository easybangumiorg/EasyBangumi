package com.heyanle.easybangumi4.ui.common.player

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.ui.common.player.utils.systemVolume
import com.heyanle.easybangumi4.ui.common.player.utils.windowBrightness
import com.heyanle.easybangumi4.utils.loge

/**
 * Created by LoliBall on 2023/3/2 11:37.
 * https://github.com/WhichWho
 */

enum class DragType {
    BRIGHTNESS, VOLUME
}

fun Modifier.brightVolume(
    showUi: MutableState<DragType?>,
    onChange: ((type: DragType) -> Unit)? = null,
): Modifier = brightVolume(
    onShowUi = { showUi.value = it; onChange?.invoke(it) },
    onHideUi = { showUi.value = null },
    callback = { type, _ -> onChange?.invoke(type) }
)

fun Modifier.brightVolume(
    onShowUi: ((type: DragType) -> Unit)? = null,
    onHideUi: ((type: DragType) -> Unit)? = null,
    callback: ((type: DragType, delta: Float) -> Unit)? = null
): Modifier = composed {
    val ctx = LocalContext.current as Activity
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    onSizeChanged { viewSize = it }
        .pointerInput("音量、亮度") {
            var type = DragType.BRIGHTNESS
            var offset = 0f // 累积偏移量
            var lastTrigger = 0 // 上次触发回调的值
            var initVolume = 0f
            var initBright = 0f
            detectVerticalDragGestures(
                onDragStart = {
                    offset = 0f
                    if (it.x >= viewSize.width / 2) { // 右 音量
                        type = DragType.VOLUME
                        initVolume = systemVolume.loge()
                    } else { // 左 亮度
                        type = DragType.BRIGHTNESS
                        initBright = ctx.windowBrightness.loge()
                    }
                    onShowUi?.invoke(type)
                },
                onDragCancel = { onHideUi?.invoke(type) },
                onDragEnd = { onHideUi?.invoke(type) },
                onVerticalDrag = { _, dragAmount ->
                    offset -= dragAmount // 上滑减少，下滑增加 坐标系是反的
                    val percent = (offset / viewSize.height) // -1f..1f
                    val triggerValue = (percent * 100).toInt() / 10 // 分成10个段
                    if (triggerValue != lastTrigger) {
                        lastTrigger = triggerValue
                        when (type) {
                            DragType.BRIGHTNESS -> ctx.windowBrightness = initBright + percent
                            DragType.VOLUME -> systemVolume = initVolume + percent
                        }
                        callback?.invoke(type, percent)
                    }
                }
            )
        }
}

@Composable
fun BrightVolumeUi(icon: ImageVector, contentDescription: String, percent: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(16.dp)
        ,
        contentAlignment = Alignment.Center
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
        ){
            Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.size(16.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(100.dp),
                progress = percent/100F
            )

        }
    }
}
