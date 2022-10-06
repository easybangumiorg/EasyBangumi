package com.heyanle.easybangumi.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Created by HeYanLe on 2022/9/11 21:01.
 * https://github.com/heyanLE
 */
class PagerAdapter(
    activity: FragmentActivity,
    private val count: Int,
    private val fragmentCreator: (position: Int) -> Fragment
):FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return count
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentCreator(position)
    }
}