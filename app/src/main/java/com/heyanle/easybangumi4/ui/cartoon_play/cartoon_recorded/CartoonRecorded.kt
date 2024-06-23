package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.clip_video.ClipVideoSeek
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.task.CartoonRecordedTaskDialog
import loli.ball.easyplayer2.ControlViewModel

/**
 * Created by heyanlin on 2024/6/21.
 */
@OptIn(UnstableApi::class)
@Composable
fun CartoonRecorded(
    controlViewModel: ControlViewModel,
    cartoonRecordedModel: CartoonRecordedModel,
    show: Boolean,
    onDismissRequire: () -> Unit
) {
    if (show) {
        Surface(
            color = Color.Black,
            contentColor = Color.White
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CartoonRecordedContent(
                    cartoonRecordedModel = cartoonRecordedModel,
                    controlViewModel = controlViewModel,
                    onDismissRequire = onDismissRequire
                )
            }
        }
    }

    val task = cartoonRecordedModel.cartoonRecordedTaskModel.value
    if (task != null){
        CartoonRecordedTaskDialog(cartoonRecordedTaskModel = task)
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun BoxScope.CartoonRecordedContent(
    cartoonRecordedModel: CartoonRecordedModel,
    controlViewModel: ControlViewModel,
    onDismissRequire: () -> Unit
) {

    LaunchedEffect(Unit) {
        cartoonRecordedModel.onLaunch()
//        launch {
//            snapshotFlow {
//                cartoonRecordedModel.videoSize to (cartoonRecordedModel.renderContainerWidth to cartoonRecordedModel.renderContainerHeight)
//            }.collectLatest {
//                cartoonRecordedModel.measureHelper.setScreenScale(MeasureHelper.SCREEN_SCALE_ADAPT)
//                cartoonRecordedModel.measureHelper.setVideoSize(it.first.width, it.first.height)
//                val res = cartoonRecordedModel.measureHelper.doMeasure(
//                    it.second.first, it.second.second
//                )
//                cartoonRecordedModel.renderWidth = res[0]
//                cartoonRecordedModel.renderHeight = res[1]
//
//                cartoonRecordedModel.clipLeft = 0f
//                cartoonRecordedModel.clipTop = 0f
//
//                cartoonRecordedModel.clipRight = cartoonRecordedModel.renderWidth.toFloat()
//                cartoonRecordedModel.clipBottom = cartoonRecordedModel.renderHeight.toFloat()
//            }
//        }
    }

    DisposableEffect(Unit) {
        // exoPlayer.clearVideoSurface()
        onDispose {
            cartoonRecordedModel.exoPlayer.seekTo(cartoonRecordedModel.currentPosition)
            cartoonRecordedModel.onDispose()
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val renderState = cartoonRecordedModel.renderState.collectAsState()

    val renderRect = renderState.value.renderRect
    val clipRect = cartoonRecordedModel.cropRect

    // Render container
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        cartoonRecordedModel.onDragStart(it)
                    },
                    onDrag = { c: PointerInputChange, dragAmount: Offset ->
                        cartoonRecordedModel.onDrag(c.position, dragAmount)
                    },
                    onDragEnd = {
                        cartoonRecordedModel.onDragEnd()
                    },
                    onDragCancel = {
                        cartoonRecordedModel.onDragEnd()
                    },
                )
            }
            .drawWithContent {
                drawContent()

                if (!clipRect.isEmpty) {


                    // 黑色半透明遮罩
                    val maskColor = Color(0x80000000)
                    drawRect(
                        color = maskColor,
                        topLeft = renderRect.topLeft,
                        size = Size(
                            renderRect.width,
                            cartoonRecordedModel.cropRect.top - renderRect.top
                        ),
                    )
                    drawRect(
                        color = maskColor,
                        topLeft = Offset(renderRect.left, cartoonRecordedModel.cropRect.top),
                        size = Size(
                            cartoonRecordedModel.cropRect.left - renderRect.left,
                            cartoonRecordedModel.cropRect.height
                        ),
                    )
                    drawRect(
                        color = maskColor,
                        topLeft = cartoonRecordedModel.cropRect.topRight,
                        size = Size(
                            renderRect.right - cartoonRecordedModel.cropRect.right,
                            cartoonRecordedModel.cropRect.height
                        ),
                    )
                    drawRect(
                        color = maskColor,
                        topLeft = Offset(renderRect.left, cartoonRecordedModel.cropRect.bottom),
                        size = Size(
                            renderRect.width,
                            renderRect.bottom - cartoonRecordedModel.cropRect.bottom
                        ),
                    )

                    // 裁剪框
                    drawRect(
                        color = Color.White,
                        topLeft = cartoonRecordedModel.cropRect.topLeft,
                        size = cartoonRecordedModel.cropRect.size,
                        style = Stroke(
                            width = 1.dp.toPx()
                        )
                    )

                    // 四角圆点

                    drawCircleWithStroke(
                        center = cartoonRecordedModel.cropRect.topLeft
                    )
                    drawCircleWithStroke(
                        center = cartoonRecordedModel.cropRect.topRight
                    )
                    drawCircleWithStroke(
                        center = cartoonRecordedModel.cropRect.bottomRight
                    )
                    drawCircleWithStroke(
                        center = cartoonRecordedModel.cropRect.bottomLeft
                    )

                    // 四边 Handler
                    drawRectCenterWithStroke(
                        center = cartoonRecordedModel.cropRect.topCenter
                    )
                    drawRectCenterWithStroke(
                        center = cartoonRecordedModel.cropRect.centerRight
                    )
                    drawRectCenterWithStroke(
                        center = cartoonRecordedModel.cropRect.bottomCenter
                    )
                    drawRectCenterWithStroke(
                        center = cartoonRecordedModel.cropRect.centerLeft
                    )
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CartoonRecordedTopAppBar(
            modifier = Modifier,
            cartoonRecordedModel = cartoonRecordedModel,
            onBack = onDismissRequire,
            onSave = {
                cartoonRecordedModel.onSave()
            }
        )
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = {
                    cartoonRecordedModel.textureView.apply {
                        (parent as? ViewGroup)?.removeView(this)
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    }
                },
                modifier = Modifier.onPlaced {
                    cartoonRecordedModel.onRenderPlace(
                        it.positionInParent() + (it.parentLayoutCoordinates?.positionInParent()?: Offset.Zero),
                        it.size
                    )
                }
            )



            if (controlViewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        ClipVideoSeek(
            modifier = Modifier
                .padding(32.dp, 0.dp),
            controlViewModel.playWhenReady,
            cartoonRecordedModel.clipVideoModel,
        ) {
            controlViewModel.onPlayPause(it)
        }



        Spacer(modifier = Modifier.size(16.dp))
    }



}

@OptIn(UnstableApi::class)
@Composable
fun CartoonRecordedTopAppBar(
    modifier: Modifier = Modifier,
    cartoonRecordedModel: CartoonRecordedModel,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(16.dp, 0.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = {
                onBack()
            }
        ) {
            // 返回按钮
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "back",
                tint = Color.White
            )
        }

        Row {
            Surface(
                shape = CircleShape,
                modifier =
                Modifier
                    .padding(2.dp, 8.dp),
                color = if (cartoonRecordedModel.isMp4) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            ) {
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            cartoonRecordedModel.changeConfigType(2)
                        }
                        .padding(8.dp, 0.dp),
                    color = if (cartoonRecordedModel.isMp4) MaterialTheme.colorScheme.onSecondaryContainer else LocalContentColor.current,
                    fontWeight = FontWeight.W900,
                    text = stringResource(com.heyanle.easy_i18n.R.string.record_video),
                    fontSize = 12.sp,
                )
            }

            Spacer(Modifier.size(8.dp))

            Surface(
                shape = CircleShape,
                modifier =
                Modifier
                    .padding(2.dp, 8.dp),
                color = if (cartoonRecordedModel.isGif) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            ) {
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            cartoonRecordedModel.changeConfigType(1)
                        }
                        .padding(8.dp, 0.dp),
                    color = if (cartoonRecordedModel.isGif) MaterialTheme.colorScheme.onSecondaryContainer else LocalContentColor.current,
                    fontWeight = FontWeight.W900,
                    text = stringResource(com.heyanle.easy_i18n.R.string.record_gif),
                    fontSize = 12.sp,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(2.dp),
            modifier =
            Modifier
                .padding(2.dp, 8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        onSave()
                    }
                    .padding(8.dp, 0.dp),
                color =  MaterialTheme.colorScheme.onSecondaryContainer ,
                fontWeight = FontWeight.W900,
                text = stringResource(com.heyanle.easy_i18n.R.string.save),
                fontSize = 12.sp,
            )
        }

    }
}



fun DrawScope.drawCircleWithStroke(
    color: Color = Color.White.copy(0.8f),
    strokeColor: Color = Color.Black.copy(0.8f),
    center: Offset,
    radius: Float = 5.dp.toPx(),
    strokeWidth: Float = 1.dp.toPx()
){
    drawCircle(
        color = color,
        center = center,
        radius = radius
    )
    drawCircle(
        color = strokeColor,
        center = center,
        radius = radius,
        style = Stroke(
            width = strokeWidth
        )
    )
}

fun DrawScope.drawRectCenterWithStroke(
    color: Color = Color.White.copy(0.8f),
    strokeColor: Color = Color.Black.copy(0.8f),
    center: Offset,
    radius: Float = 5.dp.toPx(),
    roundRadius: CornerRadius = CornerRadius(2.dp.toPx()),
    strokeWidth: Float = 1.dp.toPx()
){

    drawRoundRect(
        color = color,
        topLeft = center - Offset(radius, radius),
        size = Size(radius * 2, radius * 2),
        cornerRadius = roundRadius,
    )

    drawRoundRect(
        color = strokeColor,
        topLeft = center - Offset(radius, radius),
        size = Size(radius * 2, radius * 2),
        cornerRadius = roundRadius,
        style = Stroke(
            width = strokeWidth
        )
    )



}