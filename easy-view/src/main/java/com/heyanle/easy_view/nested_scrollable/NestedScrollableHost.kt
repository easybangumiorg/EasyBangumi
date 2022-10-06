package com.heyanle.easy_view.nested_scrollable

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.ViewConfigurationCompat
import androidx.viewpager2.widget.ViewPager2
import java.lang.IllegalStateException
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * 处理滑动冲突的 view group
 * 使用内部拦截法
 * 直接将发生冲突的孩子作为该 View 的唯一一个孩子
 * Created by HeYanLe on 2022/10/5 14:49.
 * https://github.com/heyanLE
 */
class NestedScrollableHost: FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private val childScrollable: Scrollable
    get() {
        if(childCount != 1){
            throw IllegalStateException("NestedScrollableHost can only have 1 child")
        }
        return Scrollable.of(getChildAt(0)) ?: throw IllegalStateException("NestedScrollableHost: Unsupported children")
    }



    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private fun canChildScroll(orientation: Int, delta: Float): Boolean {
        val direction = -delta.sign.toInt()
        return when(orientation){
            0 -> childScrollable.canScrollHorizontally(direction)
            else -> childScrollable.canScrollVertically(direction)
        }
    }

    private var downX = 0F
    private var downY = 0F

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // return super.onInterceptTouchEvent(ev)
        when(ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - downX
                val dy = ev.y - downY

                val scaledDx = dx.absoluteValue
                val scaledDy = dy.absoluteValue

                if(scaledDx > touchSlop || scaledDy > touchSlop){
                    // 识别为滑动
                    if(scaledDx > scaledDy){
                        // 横向滑动
                        // 孩子还能滑动
                        if(canChildScroll(LinearLayout.HORIZONTAL, dx)){
                            // 禁止父亲拦截
                            parent.requestDisallowInterceptTouchEvent(true)
                        }else{
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }else{
                        // 竖向滑动
                        if(canChildScroll(LinearLayout.VERTICAL, dy)){
                            parent.requestDisallowInterceptTouchEvent(true)
                        }else{
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                }else{
                    parent.requestDisallowInterceptTouchEvent(false)
                }

            }
            else -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }

        return super.onInterceptTouchEvent(ev)
    }



}