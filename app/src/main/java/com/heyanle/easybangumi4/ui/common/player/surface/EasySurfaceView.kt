package com.heyanle.easybangumi4.ui.common.player.surface

import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.ViewGroup
import com.heyanle.easybangumi4.ui.common.player.utils.MeasureHelper

/**
 * Created by HeYanLe on 2023/3/9 15:21.
 * https://github.com/heyanLE
 */
class EasySurfaceView: SurfaceView {

    private val measureHelper: MeasureHelper = MeasureHelper()

    fun setVideoSize(width: Int, height: Int) {
        if (width > 0 && height > 0) {
            measureHelper.setVideoSize(width, height)
            requestLayout()
        }
    }

    fun setVideoRotation(degree: Int) {
        measureHelper.setVideoRotation(degree)
        requestLayout()
    }

    fun setScaleType(scaleType: Int) {
        measureHelper.setScreenScale(scaleType)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredSize: IntArray = measureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredSize[0], measuredSize[1])
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        holder.setFormat(PixelFormat.RGBA_8888)
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}