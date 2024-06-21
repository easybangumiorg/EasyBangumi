package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.clip_video

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.utils.dip2px
import com.heyanle.easybangumi4.utils.px2dip
import loli.ball.easyplayer2.utils.pointerInput
import kotlin.math.abs

/**
 * Created by heyanlin on 2024/6/21.
 */
@OptIn(UnstableApi::class)
@Composable
fun ClipVideoSeek(
    clipVideoModel: ClipVideoModel,
) {

    val widgetState by clipVideoModel.widgetState.collectAsState()
    val clipVideoState by clipVideoModel.clipVideoState.collectAsState()
    val runtimeState by clipVideoModel.runtimeState.collectAsState()

    val clip = clipVideoState
    val runtime = runtimeState


    val pos2PxParam = runtimeState.pos2Px
    val px2PosParam = runtimeState.px2Pos

    val colorScheme = MaterialTheme.colorScheme

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .onSizeChanged {
            clipVideoModel.onSizeChange(it.width, it.height)
        }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = {
                    val diff = 200

                    val currentPx =
                        clipVideoState.selectionCurrent * pos2PxParam.first + pos2PxParam.second
                    if (abs(currentPx - it.x) < diff) {
                        clipVideoModel.onFocusChange(1)
                        return@detectHorizontalDragGestures
                    }

                    val startPx =
                        clipVideoState.selectionStart * pos2PxParam.first + pos2PxParam.second
                    if (abs(startPx - it.x) < diff) {
                        clipVideoModel.onFocusChange(2)
                        return@detectHorizontalDragGestures
                    }

                    val endPx = clipVideoState.selectionEnd * pos2PxParam.first + pos2PxParam.second
                    if (abs(endPx - it.x) < diff) {
                        clipVideoModel.onFocusChange(3)
                        return@detectHorizontalDragGestures
                    }
                },
                onHorizontalDrag = { c: PointerInputChange, _: Float ->
                    val x = c.position.x
                    val pos = x * px2PosParam.first + px2PosParam.second
                    clipVideoModel.onSelectionChange(pos, runtimeState.focusMode)
                },
                onDragEnd = {
                    clipVideoModel.onFocusChange(0)
                },
                onDragCancel = {
                    clipVideoModel.onFocusChange(0)
                }
            )
        }.drawWithContent {
            drawContent()


            drawBorder(
                color = colorScheme.primaryContainer,
                horizontalWidth = clipVideoModel.horizontalPaddingPx.toFloat(),
                verticalWidth = clipVideoModel.verticalPaddingPx.toFloat(),

                startPosition = clipVideoModel.start,
                endPosition = clipVideoModel.end,

                maxPosition = clipVideoModel.end,
                minPosition = clipVideoModel.start,
            )

            drawBorder(
                color = colorScheme.primary,
                horizontalWidth = clipVideoModel.horizontalPaddingPx.toFloat(),
                verticalWidth = clipVideoModel.verticalPaddingPx.toFloat(),

                startPosition = clip.selectionStart,
                endPosition = clip.selectionEnd,

                maxPosition = clipVideoModel.end,
                minPosition = clipVideoModel.start,

                handlerColor = colorScheme.onPrimary
            )

            val currentPx =
                clipVideoState.selectionCurrent * pos2PxParam.first + pos2PxParam.second
            if (runtimeState.focusMode == 0) {
                drawLine(
                    Color.White,
                    start = Offset(currentPx, 4.dp.toPx()),
                    end = Offset(currentPx, size.height - 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            } else if (runtimeState.focusMode == 3) {
                drawLine(
                    Color.White,
                    start = Offset(currentPx, 0f),
                    end = Offset(currentPx, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }

        }
    ) {

        if (clip.check()){
            for (p in runtimeState.jpgPositionList) {
                val entity = runtime.bmpTreeMap.lowerEntry(p) ?: runtime.bmpTreeMap.higherEntry(p)
                BlackMask(
                    enable = abs(entity.key - p) >= 5000,
                    alpha = 0.8f
                ) {
//
                    OkImage(
                        image = entity.value,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        errorColor = null,
                        errorRes = null,
                        placeholderColor = Color.Black.copy(alpha = 0.8f),
                        isGif = false,
                        crossFade = false,
                        placeholderRes = null,
                        //alpha = if (abs(entity.key - it) < 5000) 1f else 0.8f,
                        modifier = Modifier
                            .height(widgetState.clipSeekBoxHeight.px2dip().dp)
                            .width(widgetState.clipSeekBoxHeight.px2dip().dp)
                    )
                }
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

    handlerColor: Color? = null,
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
        topLeft = Offset(startPx - horizontalWidth, 0f),
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
        topLeft = Offset(
            endPx + horizontalWidth - 2 * verticalWidth,
            size.height - 2 * verticalWidth
        ),
        size = Size(verticalWidth * 2, verticalWidth * 2),
    )

    // 左下
    drawArc(
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

    if (handlerColor != null) {
        drawCircle(
            handlerColor,
            center = Offset(startPx - horizontalWidth / 2f, size.height / 2),
            radius = 2.dp.toPx(),
        )
        drawCircle(
            handlerColor,
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