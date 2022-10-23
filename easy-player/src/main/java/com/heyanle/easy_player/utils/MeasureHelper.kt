package com.heyanle.easy_player.utils

import android.view.View
import com.heyanle.easy_player.constant.ScreenScaleType

/**
 * Created by HeYanLe on 2022/10/23 15:35.
 * https://github.com/heyanLE
 */
class MeasureHelper {
    private var mVideoWidth = 0

    private var mVideoHeight = 0

    private var mCurrentScreenScale = 0

    private var mVideoRotationDegree = 0

    fun setVideoRotation(videoRotationDegree: Int) {
        mVideoRotationDegree = videoRotationDegree
    }

    fun setVideoSize(width: Int, height: Int) {
        mVideoWidth = width
        mVideoHeight = height
    }

    fun setScreenScale(screenScale: Int) {
        mCurrentScreenScale = screenScale
    }

    /**
     * 注意：VideoView的宽高一定要定死，否者以下算法不成立
     */
    fun doMeasure(widthMeasureSpecO: Int, heightMeasureSpecO: Int): IntArray {
        var widthMeasureSpec = widthMeasureSpecO
        var heightMeasureSpec = heightMeasureSpecO
        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270) { // 软解码时处理旋转信息，交换宽高
            widthMeasureSpec += heightMeasureSpec
            heightMeasureSpec = widthMeasureSpec - heightMeasureSpec
            widthMeasureSpec -= heightMeasureSpec
        }
        var width = View.MeasureSpec.getSize(widthMeasureSpec)
        var height = View.MeasureSpec.getSize(heightMeasureSpec)
        if (mVideoHeight == 0 || mVideoWidth == 0) {
            return intArrayOf(width, height)
        }
        when (mCurrentScreenScale) {
            ScreenScaleType.SCREEN_SCALE_DEFAULT -> if (mVideoWidth * height < width * mVideoHeight) {
                width = height * mVideoWidth / mVideoHeight
            } else if (mVideoWidth * height > width * mVideoHeight) {
                height = width * mVideoHeight / mVideoWidth
            }
            ScreenScaleType.SCREEN_SCALE_ORIGINAL -> {
                width = mVideoWidth
                height = mVideoHeight
            }
            ScreenScaleType.SCREEN_SCALE_16_9 -> if (height > width / 16 * 9) {
                height = width / 16 * 9
            } else {
                width = height / 9 * 16
            }
            ScreenScaleType.SCREEN_SCALE_4_3 -> if (height > width / 4 * 3) {
                height = width / 4 * 3
            } else {
                width = height / 3 * 4
            }
            ScreenScaleType.SCREEN_SCALE_MATCH_PARENT -> {
                width = widthMeasureSpec
                height = heightMeasureSpec
            }
            ScreenScaleType.SCREEN_SCALE_CENTER_CROP -> if (mVideoWidth * height > width * mVideoHeight) {
                width = height * mVideoWidth / mVideoHeight
            } else {
                height = width * mVideoHeight / mVideoWidth
            }
            else -> if (mVideoWidth * height < width * mVideoHeight) {
                width = height * mVideoWidth / mVideoHeight
            } else if (mVideoWidth * height > width * mVideoHeight) {
                height = width * mVideoHeight / mVideoWidth
            }
        }
        return intArrayOf(width, height)
    }

}