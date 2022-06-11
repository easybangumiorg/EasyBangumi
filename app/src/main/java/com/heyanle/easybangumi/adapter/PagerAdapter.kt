package com.heyanle.easybangumi.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Created by HeYanLe on 2021/9/20 15:42.
 * https://github.com/heyanLE
 */
class PagerAdapter(
    activity: AppCompatActivity,
    private val count: Int,
    private val fragmentCreator: (position: Int) -> Fragment
)
    : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return count
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentCreator(position)
    }
}