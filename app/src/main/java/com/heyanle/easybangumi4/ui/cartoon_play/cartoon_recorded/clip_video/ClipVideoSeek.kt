package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.clip_video

import android.graphics.drawable.shapes.RoundRectShape
import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.heyanle.easybangumi4.exo.thumbnail.TagFile
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.utils.dip2px
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.px2dip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.utils.loge
import loli.ball.easyplayer2.utils.pointerInput
import kotlin.math.abs

/**
 * Created by heyanle on 2024/6/16.
 * https://github.com/heyanLE
 */
@OptIn(UnstableApi::class)
@Composable
fun ClipVideoSeek(
    state: ClipVideoState
) {

    LaunchedEffect(key1 = Unit) {
        state.onLaunch()
        launch {
            snapshotFlow {
                Triple(state.width, state.height, state.videoSize) to (state.start to state.end)
            }.collectLatest {
                if (!state.check()) {
                    return@collectLatest
                }
                val duringPx =
                    (it.first.third.width * it.first.second / it.first.third.height.toFloat())
                val positionPx =
                    (duringPx * (it.second.second - it.second.first) / it.first.first).toLong()
                val arrayList = arrayListOf<Long>()

                var current = it.second.first
                while (current <= it.second.second) {
                    arrayList += current
                    current += positionPx
                }

                state.jpgPositionList = arrayList.toList()
            }
        }
    }



    DisposableEffect(key1 = Unit) {
        onDispose {
            state.onDispose()
        }
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
        .background(Color.Black)
        .onSizeChanged {
            state.onViseChange(it.width, it.height)
        }
        .pointerInput("", true) {
            detectHorizontalDragGestures(
                onDragStart = {
                    val diff = 200
                    // current
                    val currentPx =
                        ((state.currentPosition - state.start) * (state.width - 2 * state.horizontalPadding) / (state.end - state.start)) + state.horizontalPadding
                    if (abs(currentPx - it.x) < diff) {
                        state.focusMode = 3
                        return@detectHorizontalDragGestures
                    }

                    // start
                    val startPx =
                        ((state.selectionStart - state.start) * (state.width - 2 * state.horizontalPadding) / (state.end - state.start)) + state.horizontalPadding
                    if (abs(startPx - state.horizontalPadding/2f - it.x) < diff) {
                        state.focusMode = 1
                        return@detectHorizontalDragGestures
                    }

                    // end
                    val endPx =
                        ((state.selectionEnd + state.horizontalPadding/2f - state.start) * (state.width - 2 * state.horizontalPadding) / (state.end - state.start)) + state.horizontalPadding
                    if (abs(endPx - it.x) < diff) {
                        state.focusMode = 2
                        return@detectHorizontalDragGestures
                    }
                },
                onDragEnd = {
                    state.focusMode = 0
                },
                onDragCancel = {
                    state.focusMode = 0
                },
                onHorizontalDrag = { c: PointerInputChange, dragAmount: Float ->
                    val currPx = c.position.x
                    if (!state.check()) {
                        return@detectHorizontalDragGestures
                    }
                    val curPosition =
                        (state.start + (state.end - state.start) * (currPx - state.horizontalPadding) / (state.width - 2 * state.horizontalPadding)).toLong()
                    when (state.focusMode) {
                        1 -> {
                            state.selectionStart = curPosition
                            state.check()
                        }

                        2 -> {
                            state.selectionEnd = curPosition
                            state.check()
                        }

                        3 -> {
                            state.currentPosition = curPosition
                            state.check()
                        }
                    }

                }
            )
        }
    ) {

        if (state.check()) {


            // 底部缩略图
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()


                        drawBorder(
                            color = Color(0xFF242424.toInt()),
                            horizontalWidth = state.horizontalPadding.toFloat(),
                            verticalWidth = state.verticalPadding.toFloat(),

                            startPosition = state.start,
                            endPosition = state.end,

                            maxPosition = state.end,
                            minPosition = state.start,
                            hasHandler = false
                        )

                        drawBorder(
                            color = Color(0xFFFFCF17.toInt()),
                            horizontalWidth = state.horizontalPadding.toFloat(),
                            verticalWidth = state.verticalPadding.toFloat(),
                            startPosition = state.selectionStart,
                            endPosition = state.selectionEnd,
                            maxPosition = state.end,
                            minPosition = state.start,
                            hasHandler = true
                        )

                        val currentPx =
                            ((size.width * (state.currentPosition - state.start)) / (state.end - state.start).toFloat())
                        if (state.focusMode == 0) {
                            drawLine(
                                Color.White,
                                start = Offset(currentPx, 4.dp.toPx()),
                                end = Offset(currentPx, size.height - 4.dp.toPx()),
                                strokeWidth = 2.dp.toPx()
                            )
                        } else if (state.focusMode == 3) {
                            drawLine(
                                Color.White,
                                start = Offset(currentPx, 0f),
                                end = Offset(currentPx, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                    .padding(
                        state.horizontalPadding
                            .toFloat()
                            .px2dip().dp,
                        state.verticalPadding
                            .toFloat()
                            .px2dip().dp
                    ), horizontalArrangement = Arrangement.Start
            ) {
                state.jpgPositionList.forEach {
                    val entity = state.bmpTreeMap.lowerEntry(it) ?: state.bmpTreeMap.higherEntry(it)
                    if (entity != null) {
                        BlackMask(enable = abs(entity.key - it) >= 5000, alpha = 0.8f) {
//                            if (entity.value is TagFile) {
//                                (entity.value as? TagFile)?.tag?.logi("ClipVideoSeek")
//                            }
                            OkImage(
                                image = entity.value,
                                contentDescription = "",
                                contentScale = ContentScale.FillHeight,
                                errorColor = null,
                                errorRes = null,
                                placeholderColor = null,
                                isGif = false,
                                crossFade = false,
                                placeholderRes = null,
                                //alpha = if (abs(entity.key - it) < 5000) 1f else 0.8f,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    //.alpha(if (abs(entity.key - it) < 5000) 1f else 0.5f)
                                    .aspectRatio(state.videoSize.width / state.videoSize.height.toFloat())
                            )
                        }

                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }



        }
    }

}

private fun DrawScope.drawBorder(
    color: Color,
    horizontalWidth: Float = 48.dp.toPx(),
    verticalWidth: Float = 12.dp.toPx(),

    startPosition: Long,
    endPosition: Long,

    minPosition: Long,
    maxPosition: Long,

    hasHandler: Boolean,
) {
    val allDuringPosition = maxPosition - minPosition
    val minPx = horizontalWidth.toFloat()
    val maxPx = size.width - horizontalWidth.toFloat()
    val duringPx = maxPx - minPx

    val startPx = (duringPx * (startPosition - minPosition) / allDuringPosition) + minPx
    val endPx = (duringPx * (endPosition - minPosition) / allDuringPosition) + minPx

    // 因为 Canvas 不支持不同圆角的圆角矩形，同时 Clean 模式会填充黑色，因此直接通过画扇形模拟圆弧

    // 左上
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 90f,
        useCenter = true,
        topLeft = Offset(startPx - horizontalWidth , 0f),
        size = Size(verticalWidth * 2, verticalWidth * 2),
    )

    //右上
    drawArc(
        color = color,
        startAngle = 270f,
        sweepAngle = 90f,
        useCenter = true,
        topLeft = Offset(endPx + horizontalWidth - 2 * verticalWidth, 0f),
        size = Size(verticalWidth * 2, verticalWidth * 2),
    )

    // 右下
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 90f,
        useCenter = true,
        topLeft = Offset(endPx + horizontalWidth - 2 * verticalWidth, size.height - 2 * verticalWidth),
        size = Size(verticalWidth * 2, verticalWidth * 2),
    )

    // 左下
    drawArc (
        color = color,
        startAngle = 90f,
        sweepAngle = 90f,
        useCenter = true,
        topLeft = Offset(startPx - horizontalWidth, size.height - 2 * verticalWidth),
        size = Size(verticalWidth * 2, verticalWidth * 2),
    )

    // 左侧 handler
    drawRect(
        color = color,
        topLeft = Offset(startPx - horizontalWidth, verticalWidth),
        size = Size(horizontalWidth, size.height - 2 * verticalWidth),
    )

    // 右侧 handler
    drawRect(
        color = color,
        topLeft = Offset(endPx, verticalWidth),
        size = Size(horizontalWidth, size.height - 2 * verticalWidth),
    )

    // 顶部横线
    drawRect(
        color = color,
        topLeft = Offset(startPx - horizontalWidth + verticalWidth, 0f),
        size = Size(endPx - startPx + 2 * horizontalWidth - 2 * verticalWidth, verticalWidth),
    )

    // 底部横线
    drawRect(
        color = color,
        topLeft = Offset(startPx - horizontalWidth + verticalWidth, size.height - verticalWidth),
        size = Size(endPx - startPx + 2 * horizontalWidth - 2 * verticalWidth, verticalWidth),
    )

    if (hasHandler) {
        drawCircle(
            Color.Black,
            center = Offset(startPx - horizontalWidth / 2f, size.height / 2),
            radius = 2.dp.toPx(),
        )
        drawCircle(
            Color.Black,
            center = Offset(endPx + horizontalWidth / 2f, size.height / 2),
            radius = 2.dp.toPx(),
        )
    }


}

@Composable
fun BlackMask(
    enable: Boolean,
    alpha: Float,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .width(IntrinsicSize.Min)
    ) {
        if (enable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha))
            )
        }
        content()
    }
}