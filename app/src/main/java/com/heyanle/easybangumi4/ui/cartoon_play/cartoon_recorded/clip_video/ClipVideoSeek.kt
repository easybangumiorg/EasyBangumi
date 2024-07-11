package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.clip_video

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
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
import androidx.compose.ui.unit.times
import androidx.media3.common.util.UnstableApi
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.detectHorizontalDragGesturesWithDown
import com.heyanle.easybangumi4.utils.dip2px
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.px2dip
import loli.ball.easyplayer2.utils.pointerInput
import java.util.Locale
import kotlin.math.abs

/**
 * Created by heyanlin on 2024/6/21.
 */
@OptIn(UnstableApi::class)
@Composable
fun ClipVideoSeek(
    modifier: Modifier = Modifier,
    playWhenReady: Boolean,
    clipVideoModel: ClipVideoModel,
    onPlayWhenReadyChange: (Boolean) -> Unit,
) {


    val colorScheme = MaterialTheme.colorScheme



    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                if (playWhenReady) Icons.Filled.Pause
                else Icons.Filled.PlayArrow,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        onPlayWhenReadyChange(!playWhenReady)
                    }
                    .padding(4.dp),
                //tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = null
            )

            Text(
                clipVideoModel.getShowTime(),
                //color = MaterialTheme.colorScheme.onBackground
            )


        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ClipVideoModel.seekBarHeightDp.dp)
                .onSizeChanged {
                    clipVideoModel.onSeekBarSizeChange(it.width, it.height)
                }

        ) {


            if (clipVideoModel.check()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            ClipVideoModel.horizontalPaddingDp.dp,
                            ClipVideoModel.verticalPaddingDp.dp
                        )

                ) {
                    for (p in clipVideoModel.jpgPositionList) {
                        val entity = clipVideoModel.bmpTreeMap.lowerEntry(p)
                            ?: clipVideoModel.bmpTreeMap.higherEntry(p)
                        BlackMask(
                            enable = abs((entity?.key ?: Long.MAX_VALUE) - p) >= 5000,
                            alpha = 0.8f
                        ) {
                            OkImage(
                                image = entity?.value,
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                errorColor = null,
                                errorRes = null,
                                placeholderColor = null,
                                isGif = false,
                                crossFade = false,
                                placeholderRes = null,
                                //alpha = if (abs(entity.key - it) < 5000) 1f else 0.8f,
                                modifier = Modifier
                                    .height(ClipVideoModel.seekBarHeightDp.dp - 2 * ClipVideoModel.verticalPaddingDp.dp)
                                    .width(ClipVideoModel.seekBarHeightDp.dp - 2 * ClipVideoModel.verticalPaddingDp.dp)
                            )
                        }
                    }
                }

            }
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    var down: Offset? = null
                    detectHorizontalDragGesturesWithDown(
                        onDown = {
                            down = it
                        },
                        onDragStart = {
                            val diff = 100
                            val curPosition = down ?: it

                            val startPx =
                                clipVideoModel.selectionStart * clipVideoModel.pos2Px.first + clipVideoModel.pos2Px.second
                            if (curPosition.x in startPx - clipVideoModel.horizontalPaddingPx - diff..startPx) {
                                clipVideoModel.onFocusChange(1)
                                return@detectHorizontalDragGesturesWithDown
                            }

                            val endPx =
                                clipVideoModel.selectionEnd * clipVideoModel.pos2Px.first + clipVideoModel.pos2Px.second
                            if (curPosition.x in endPx..endPx + clipVideoModel.horizontalPaddingPx + diff) {
                                clipVideoModel.onFocusChange(2)
                                return@detectHorizontalDragGesturesWithDown
                            }

                            val pointPos =
                                clipVideoModel.px2Pos.first * curPosition.x + clipVideoModel.px2Pos.second
                            if (pointPos >= clipVideoModel.selectionStart && pointPos <= clipVideoModel.selectionEnd) {
                                clipVideoModel.onFocusChange(3)
                                return@detectHorizontalDragGesturesWithDown
                            }
                        },
                        onHorizontalDrag = { c: PointerInputChange, _: Float ->
                            val x = c.position.x
                            val pos = x * clipVideoModel.px2Pos.first + clipVideoModel.px2Pos.second
                            //"$x $pos".logi("ClipVideoSeek")
                            clipVideoModel.onSelectionChange(pos, clipVideoModel.focusMode)
                        },
                        onDragEnd = {
                            clipVideoModel.onFocusChange(0)
                            down = null
                        },
                        onDragCancel = {
                            clipVideoModel.onFocusChange(0)
                            down = null
                        }
                    )
                }
                .drawWithCache {
                    onDrawWithContent {
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

                            startPosition = clipVideoModel.selectionStart,
                            endPosition = clipVideoModel.selectionEnd,

                            maxPosition = clipVideoModel.end,
                            minPosition = clipVideoModel.start,

                            handlerColor = colorScheme.onPrimary
                        )

                        val currentPx =
                            clipVideoModel.selectionCurrent * clipVideoModel.pos2Px.first + clipVideoModel.pos2Px.second
                        if (clipVideoModel.focusMode == 0) {
                            drawLine(
                                Color.White,
                                start = Offset(currentPx, 4.dp.toPx()),
                                end = Offset(currentPx, size.height - 4.dp.toPx()),
                                strokeWidth = 2.dp.toPx()
                            )
                        } else if (clipVideoModel.focusMode == 3) {
                            drawLine(
                                Color.White,
                                start = Offset(currentPx, 0f),
                                end = Offset(currentPx, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                })
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