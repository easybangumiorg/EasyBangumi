package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Created by heyanlin on 2024/6/11.
 */
class RectSelectionView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val rectF = RectF()

    private var focus: Int = 0





    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        rectF.left = 0F
        rectF.top = 0F
        rectF.right = width.toFloat()
        rectF.bottom = height.toFloat()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                if (focus == 0) {
                    val dlt = event.x - rectF.left
                    val drt = rectF.right - event.x
                    val dlb = event.y - rectF.top
                    val drb = rectF.bottom - event.y

                }
            }
            MotionEvent.ACTION_MOVE -> {
                when(focus){
                    1 -> {
                        leftTop.x = event.x
                        leftTop.y = event.y
                        invalidate()
                    }
                    2 -> {
                        rightTop.x = event.x
                        rightTop.y = event.y
                        invalidate()
                    }
                    3 -> {
                        leftBottom.x = event.x
                        leftBottom.y = event.y
                        invalidate()
                    }
                    4 -> {
                        rightBottom.x = event.x
                        rightBottom.y = event.y
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                focus = 0
            }
        }
        return true
    }

    private fun PointF.distance(x: Float, y: Float): Float {
        val dx = abs(x - this.x)
        val dy = abs(y - this.y)
        return  sqrt(dx * dx + dy * dy)
    }
}