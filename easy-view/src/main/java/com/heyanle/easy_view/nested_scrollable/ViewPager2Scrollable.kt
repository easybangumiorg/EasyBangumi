package com.heyanle.easy_view.nested_scrollable

import androidx.viewpager2.widget.ViewPager2

/**
 * Created by HeYanLe on 2022/10/5 15:07.
 * https://github.com/heyanLE
 */
class ViewPager2Scrollable(
    private val viewPager2: ViewPager2
): Scrollable {
    override fun canScrollHorizontally(direction: Int): Boolean {
        return viewPager2.canScrollHorizontally(direction)
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return viewPager2.canScrollVertically(direction)
    }
}