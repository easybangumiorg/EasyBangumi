package com.heyanle.easy_view.nested_scrollable

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

/**
 * Created by HeYanLe on 2022/10/5 14:38.
 * https://github.com/heyanLE
 */
interface Scrollable {

    companion object {
        fun of(view: View): Scrollable? {
            return when(view){
                is RecyclerView -> RecyclerScrollable(view)
                is ViewPager2 -> ViewPager2Scrollable(view)
                else -> null
            }
        }
    }

    /**
     * 横向能否滑动
     * @param direction 方向 正数向右
     */
    fun canScrollHorizontally(direction: Int): Boolean

    /**
     * 竖向能否滑动
     */
    fun canScrollVertically(direction: Int): Boolean



}