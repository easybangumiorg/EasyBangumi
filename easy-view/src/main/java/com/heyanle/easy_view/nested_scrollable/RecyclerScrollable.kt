package com.heyanle.easy_view.nested_scrollable

import androidx.recyclerview.widget.RecyclerView

/**
 * Created by HeYanLe on 2022/10/5 14:46.
 * https://github.com/heyanLE
 */
class RecyclerScrollable(
    private val recyclerView: RecyclerView
): Scrollable {
    override fun canScrollHorizontally(direction: Int): Boolean {
        return recyclerView.canScrollHorizontally(direction)
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return recyclerView.canScrollVertically(direction)
    }

}