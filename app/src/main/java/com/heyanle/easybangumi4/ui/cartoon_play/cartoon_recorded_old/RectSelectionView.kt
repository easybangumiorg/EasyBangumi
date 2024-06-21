package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded_old

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Created by heyanlin on 2024/6/11.
 */
class RectSelectionView : View {

    companion object {
        const val MEED_POINT_DIFFERENCE = 50F
        const val POINT_RADIUS = 10F
        const val INLINE_RECT_WIDTH = 5F
    }

    private val rectF = RectF()
    private val blackMaskPaint = Paint()
        .apply {
            isAntiAlias = true
            color = 0x80000000.toInt()
            style = Paint.Style.FILL
        }
    private val writePointPaint = Paint()
        .apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
        }
    private val inlineRectPaint = Paint()
        .apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.STROKE
            strokeWidth = INLINE_RECT_WIDTH
        }
    private var focus: Int = 0


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, rectF.right, rectF.top, blackMaskPaint)
        canvas.drawRect(rectF.right, 0f, width.toFloat(), rectF.bottom, blackMaskPaint)
        canvas.drawRect(0f, rectF.top, rectF.left, height.toFloat(), blackMaskPaint)
        canvas.drawRect(rectF.left, rectF.bottom, width.toFloat(), height.toFloat(), blackMaskPaint)

        canvas.drawRect(rectF, inlineRectPaint)

        canvas.drawCircle(rectF.left, rectF.top, POINT_RADIUS, writePointPaint)
        canvas.drawCircle(rectF.right, rectF.top, POINT_RADIUS, writePointPaint)
        canvas.drawCircle(rectF.left, rectF.bottom, POINT_RADIUS, writePointPaint)
        canvas.drawCircle(rectF.right, rectF.bottom, POINT_RADIUS, writePointPaint)


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
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (focus == 0) {
                    val dlt = event.x - rectF.left
                    val drt = rectF.right - event.x
                    val dlb = event.y - rectF.top
                    val drb = rectF.bottom - event.y

                    // 左上 左下
                    if (abs(dlt) < MEED_POINT_DIFFERENCE) {
                        // 左上
                        focus = if (abs(dlb) < MEED_POINT_DIFFERENCE) {
                            1
                        } else if (abs(drb) < MEED_POINT_DIFFERENCE) {
                            // 左下
                            3
                        } else {
                            0
                        }
                    } else if (abs(drt) < MEED_POINT_DIFFERENCE) {
                        // 右上 右下
                        focus = if (abs(dlb) < MEED_POINT_DIFFERENCE) {
                            2
                        } else if (abs(drb) < MEED_POINT_DIFFERENCE){
                            4
                        } else {
                            0
                        }
                    }

                    if (focus == 0){
                        return false
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (focus) {
                    1 -> {
                        rectF.left = event.x
                        rectF.top = event.y
                        invalidate()
                    }

                    2 -> {
                        rectF.right = event.x
                        rectF.top = event.y
                        invalidate()
                    }

                    3 -> {
                        rectF.left = event.x
                        rectF.bottom = event.y
                        invalidate()
                    }

                    4 -> {
                        rectF.right = event.x
                        rectF.bottom = event.y
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

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

}