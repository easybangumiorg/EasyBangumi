package com.heyanle.easybangumi.ui.main.diy

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import kotlin.math.abs
import android.view.VelocityTracker
import kotlin.math.min


/**
 * Created by HeYanLe on 2021/9/21 12:30.
 * https://github.com/heyanLE
 */
class HomeColumnHorizontalScrollView : HorizontalScrollView {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
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

    private var initialTouchX = 0F
    private var initialTouchY = 0F

    var isMove = false

    var homeColumnLinearLayout: HomeColumnLinearLayout? = null




    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //Log.e("asdfg", "dispatchTouchEvent ${ev?.action}")
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                isMove = false
                initialTouchX = ev.x
                initialTouchY = ev.y
                //parent.requestDisallowInterceptTouchEvent(true)
                //parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                isMove = true
                val dx = ev.x - initialTouchX
                val dy = ev.y - initialTouchY

                val hasScrollView = (canScrollHorizontally(-1) && dx > 0)
                        || (canScrollHorizontally(1) && dx < 0)
                val r = abs(dy) / abs(dx)
                if (r < 1f && hasScrollView) { // 比例可调整
                    requestDisallowInterceptTouchEvent(true)

                }
            }
            MotionEvent.ACTION_UP -> {
                requestDisallowInterceptTouchEvent(false)
                if(!isMove){
                    homeColumnLinearLayout?.onTouchUpWithoutMove?.let { it() }
                }
                isMove = false

            }
            MotionEvent.ACTION_CANCEL -> {
                requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.dispatchTouchEvent(ev).apply {
            Log.i("HomeColumn", "$this")
        }

    }



}