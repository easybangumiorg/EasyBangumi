package org.easybangumi.next.libplayer.exoplayer

import android.view.View
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.libplayer.api.VideoSize
import kotlin.math.roundToInt

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class MeasureHelper {

    private var videoSize = IntSize.Zero
    private var viewSize = IntSize.Zero
    private var scaleType: C.RendererScaleType = C.RendererScaleType.SCALE_ADAPT
    

    var frameOffset = IntOffset.Zero
        private set

    var frameSize = IntSize.Zero
        private set

    fun setVideoSize(size: VideoSize) {
        size.logicSize ?.let {
            if (videoSize.width != it.first || videoSize.height != it.second) {
                videoSize = IntSize(it.first, it.second)
            }
        }
    }

    fun doOnMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        var width = View.MeasureSpec.getSize(widthMeasureSpec)
        var height = View.MeasureSpec.getSize(heightMeasureSpec)
        if (width == 0 || height == 0) {
            videoSize = IntSize.Zero
            calculate()
            return
        }
        viewSize = IntSize(width, height)
        calculate()
    }

    fun setScaleType(type: C.RendererScaleType) {
        if (scaleType != type) {
            scaleType = type
        }
    }

    // 懒加载
    private val pair16r9: Pair<Int, Int> by lazy {
        Pair(16, 9)
    }
    private val pair4r3: Pair<Int, Int> by lazy {
        Pair(4, 3)
    }


    fun calculate() {
        when (scaleType) {
            // 原尺寸居中输出，不考虑遮挡
            C.RendererScaleType.SCALE_SOURCE -> {
                val xx = viewSize.width - videoSize.width
                val yy = viewSize.height - videoSize.height
                frameOffset = IntOffset((xx / 2f).roundToInt(), (yy / 2f).roundToInt())
                frameSize = IntSize(videoSize.width, videoSize.height)
            }


            C.RendererScaleType.SCALE_16_9 -> {
                centerAdapt(pair16r9)
            }
            C.RendererScaleType.SCALE_4_3 -> {
                centerAdapt(pair4r3)
            }
            C.RendererScaleType.SCALE_ADAPT -> {
//                println("${videoSize}")
                centerAdapt(videoSize.width to videoSize.height)
            }


            C.RendererScaleType.SCALE_MATCH_PARENT -> {
                frameOffset = IntOffset(0, 0)
                frameSize = IntSize(videoSize.width, viewSize.height)

            }
            C.RendererScaleType.SCALE_CENTER_CROP -> {
                // 平铺，从中心裁切，保证占满屏幕
                // 和 centerAdapt 相反
                if (viewSize.width * videoSize.height > viewSize.height * videoSize.width) {
                    // 宽度为准
                    val width = viewSize.width
                    val height =( width * videoSize.height / videoSize.width)
                    frameOffset = IntOffset(0, ((viewSize.height - height) / 2f).roundToInt())
                    frameSize = IntSize(width, height)
                } else {
                    // 高度为准
                    val height = viewSize.height
                    val width = height * videoSize.width / videoSize.height
                    frameOffset = IntOffset(((viewSize.width - width) / 2f).roundToInt(), 0)
                    frameSize = IntSize(width, height)
                }

            }
            C.RendererScaleType.SCALE_FOR_HEIGHT -> {
                // 以高度为准
                val height = viewSize.height
                val width = height * videoSize.width / videoSize.height
                frameOffset = IntOffset(((viewSize.width - width) / 2f).roundToInt(), 0)
                frameSize = IntSize(width, height)

            }
            C.RendererScaleType.SCALE_FOR_WIDTH -> {
                // 以宽度为准
                val width = viewSize.width
                val height = width * videoSize.height / videoSize.width
                frameOffset = IntOffset(0, ((viewSize.height - height) / 2f).roundToInt())
                frameSize = IntSize(width, height)
            }
        }



    }

    // 居中适应屏幕，保持宽高比
    private fun centerAdapt(
        radio: Pair<Int, Int>,
    ) {
        if (radio.first <= 0 || radio.second <= 0) {
            frameSize = viewSize
            frameOffset = IntOffset.Zero
        } else {
//            println(videoSize.toString() + " " + viewSize.toString() + " " + radio.toString())
            if (viewSize.width * radio.second <= viewSize.height * radio.first) {
                // 宽度为准
                val width = viewSize.width
                val height = width * radio.second / radio.first
                frameOffset = IntOffset(0, ((viewSize.height - height) / 2f).roundToInt())
                frameSize = IntSize(width, height)
            } else {
                // 高度为准
                val height = viewSize.height
                val width = height * radio.first / radio.second
                frameOffset = IntOffset(((viewSize.width - width) / 2f).roundToInt(), 0)
                frameSize = IntSize(width, height)
            }
        }

    }
}