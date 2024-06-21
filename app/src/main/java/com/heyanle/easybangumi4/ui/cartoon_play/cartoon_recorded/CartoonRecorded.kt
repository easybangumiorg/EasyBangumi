package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.clip_video.ClipVideoSeek
import com.heyanle.easybangumi4.utils.dip2px
import com.heyanle.easybangumi4.utils.px2dip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.ControlViewModel
import loli.ball.easyplayer2.utils.MeasureHelper
import loli.ball.easyplayer2.utils.pointerInput

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
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
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



    Column {
        CartoonRecordedTopAppBar(
            cartoonRecordedModel = cartoonRecordedModel,
            onBack = onDismissRequire,
            onSave = {

            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onSizeChanged {
                    cartoonRecordedModel.onRenderSizeContainerChange(it.width, it.height)
                },
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = {
                    cartoonRecordedModel.textureView.apply {
                        (parent as? ViewGroup)?.removeView(this)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.align(Alignment.Center).onSizeChanged {
                    cartoonRecordedModel.renderHeight = it.height
                    cartoonRecordedModel.renderWidth = it.width

                    cartoonRecordedModel.clipLeft = 0f
                    cartoonRecordedModel.clipTop = 0f

                    cartoonRecordedModel.clipRight = it.width.toFloat()
                    cartoonRecordedModel.clipBottom = it.height.toFloat()
                }
            )


            val width = cartoonRecordedModel.renderWidth
            val height = cartoonRecordedModel.renderHeight

            if (width > 0 && height > 0) {
                Canvas(
                    modifier = Modifier
                        .height(height.px2dip().dp)
                        .width(width.px2dip().dp)
                        .align(Alignment.Center)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    cartoonRecordedModel.onDragStart(it.x, it.y)
                                },
                                onDrag = { c: PointerInputChange, _ ->
                                    cartoonRecordedModel.onDrag(c.position.x, c.position.y)
                                },
                                onDragEnd = {
                                    cartoonRecordedModel.onDragEnd()
                                },
                                onDragCancel = {
                                    cartoonRecordedModel.onDragEnd()
                                },
                            )
                        }
                ) {
                    // 裁剪框
                    drawRect(
                        color = colorScheme.onBackground,
                        topLeft = Offset(cartoonRecordedModel.clipLeft, cartoonRecordedModel.clipTop),
                        size = Size(cartoonRecordedModel.clipWidth, cartoonRecordedModel.clipHeight),
                        style = Stroke(
                            width = 1.dp.toPx()
                        )
                    )

                    // 黑色半透明遮罩
                    val maskColor = Color(0x80000000)
                    drawRect(
                        color = maskColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(width.toFloat(), cartoonRecordedModel.clipTop),
                    )
                    drawRect(
                        color = maskColor,
                        topLeft = Offset(0f, cartoonRecordedModel.clipTop),
                        size = Size(cartoonRecordedModel.clipLeft, cartoonRecordedModel.clipHeight),
                    )
                    drawRect(
                        color = maskColor,
                        topLeft = Offset(cartoonRecordedModel.clipRight, cartoonRecordedModel.clipTop),
                        size = Size(width.toFloat() - cartoonRecordedModel.clipRight, cartoonRecordedModel.clipHeight),
                    )
                    drawRect(
                        color = maskColor,
                        topLeft = Offset(0f, cartoonRecordedModel.clipBottom),
                        size = Size(width.toFloat(), height.toFloat() - cartoonRecordedModel.clipBottom),
                    )

                    // 右下角白色圆点
                    drawCircle(
                        color = colorScheme.primary,
                        center = Offset(cartoonRecordedModel.clipRight, cartoonRecordedModel.clipBottom),
                        radius = 5.dp.toPx()
                    )
                }
            }


            if (controlViewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }


        }

        ClipVideoSeek(
            controlViewModel.playWhenReady,
            cartoonRecordedModel.clipVideoModel,
        ) {
            controlViewModel.onPlayPause(it)
        }

    }


}

@OptIn(UnstableApi::class)
@Composable
fun CartoonRecordedTopAppBar(
    cartoonRecordedModel: CartoonRecordedModel,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
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
                contentDescription = "back"
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
                    color = if (cartoonRecordedModel.isMp4) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
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
                    color = if (cartoonRecordedModel.isGif) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.W900,
                    text = stringResource(com.heyanle.easy_i18n.R.string.record_gif),
                    fontSize = 12.sp,
                )
            }
        }


        TextButton(
            onClick = {
                onSave()
            }
        ) {
            Text("保存")
        }
    }
}