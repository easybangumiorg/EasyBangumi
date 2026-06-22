package loli.ball.easyplayer2.utils

import android.util.Log
import android.view.View
import kotlin.math.abs

/**
 * Created by HeYanLe on 2022/10/23 15:35.
 * https://github.com/heyanLE
 */
class MeasureHelper {

    companion object {
        const val SCREEN_SCALE_DEFAULT = 0          // 默认
        const val SCREEN_SCALE_16_9 = 1             // 16/9
        const val SCREEN_SCALE_4_3 = 2              // 4/3
        const val SCREEN_SCALE_MATCH_PARENT = 3     // 拉伸
        const val SCREEN_SCALE_ORIGINAL = 4         // 原始大小
        const val SCREEN_SCALE_CENTER_CROP = 5      // 平铺，从中心裁切，保证占满屏幕
        const val SCREEN_SCALE_ADAPT = 6            // 保证长或宽与屏幕相等，比例不变
        const val SCREEN_SCALE_FOR_HEIGHT = 7       // 以高度为准
    }

    private var mVideoWidth = 0

    private var mVideoHeight = 0

    private var mCurrentScreenScale = SCREEN_SCALE_DEFAULT

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
            widthMeasureSpec = heightMeasureSpecO
            heightMeasureSpec = widthMeasureSpecO
        }
        var width = View.MeasureSpec.getSize(widthMeasureSpec)
        var height = View.MeasureSpec.getSize(heightMeasureSpec)
        "$width $height".loge("MeasureHelper")
        if (mVideoHeight == 0 || mVideoWidth == 0) {
            return intArrayOf(width, height)
        }
        // mVideoWidth / width > mVideoHeight / height
        val widthScaleGtHeightScale = mVideoWidth * height > width * mVideoHeight
        when (mCurrentScreenScale) {
            SCREEN_SCALE_DEFAULT -> {
                if (widthScaleGtHeightScale) {
                    height = width * mVideoHeight / mVideoWidth
                } else {
                    width = height * mVideoWidth / mVideoHeight
                }
            }
            SCREEN_SCALE_ORIGINAL -> {
                width = mVideoWidth
                height = mVideoHeight
            }
            SCREEN_SCALE_16_9 -> {
                if (height > width / 16 * 9) {
                    height = width / 16 * 9
                } else {
                    width = height / 9 * 16
                }
            }
            SCREEN_SCALE_4_3 -> {
                if (height > width / 4 * 3) {
                    height = width / 4 * 3
                } else {
                    width = height / 3 * 4
                }
            }
            SCREEN_SCALE_MATCH_PARENT -> {
                width = widthMeasureSpec
                height = heightMeasureSpec
            }
            SCREEN_SCALE_CENTER_CROP -> {
                if (widthScaleGtHeightScale) {
                    width = height * mVideoWidth / mVideoHeight
                } else {
                    height = width * mVideoHeight / mVideoWidth
                }
            }
            SCREEN_SCALE_ADAPT -> {
                val wScale = width / mVideoWidth.toFloat()
                val hScale = height / mVideoHeight.toFloat()
                val scale = if (abs(wScale - 1) < abs(hScale - 1)) wScale else hScale
                width = (scale * mVideoWidth).toInt()
                height = (scale * mVideoHeight).toInt()
            }
            SCREEN_SCALE_FOR_HEIGHT -> {
                height = heightMeasureSpec
                width = height * mVideoWidth / mVideoHeight
            }
            else -> {
                if (widthScaleGtHeightScale) {
                    height = width * mVideoHeight / mVideoWidth
                } else {
                    width = height * mVideoWidth / mVideoHeight
                }
            }
        }
        return intArrayOf(width, height)
    }

}