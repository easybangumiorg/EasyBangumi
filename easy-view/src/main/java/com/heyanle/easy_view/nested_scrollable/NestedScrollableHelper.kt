package com.heyanle.easy_view.nested_scrollable

import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * 处理滑动冲突的代理帮助类
 * 使用内部拦截法
 * 直接代理对应方法即可
 * Created by HeYanLe on 2022/10/5 15:28.
 * https://github.com/heyanLE
 */
class NestedScrollableHelper(
    view: View
) {
    private val scrollable = Scrollable.of(view)
    private val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop

    private fun canChildScroll(orientation: Int, delta: Float): Boolean {
        val direction = -delta.sign.toInt()
        return when(orientation){
            0 -> scrollable?.canScrollHorizontally(direction)
            else -> scrollable?.canScrollVertically(direction)
        }?:false
    }

    private var downX = 0F
    private var downY = 0F

    /**
     * 代理该方法即可
     */
    fun onInterceptTouchEvent(ev: MotionEvent, parent: ViewGroup){
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
            else -> parent.requestDisallowInterceptTouchEvent(false)
        }
    }
}