package com.heyanle.easybangumi.anim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.adapter.PagerAdapter
import com.heyanle.easybangumi.anim.home.AnimHomeFragment
import com.heyanle.easybangumi.anim.mine.AnimMineFragment
import com.heyanle.easybangumi.anim.search.SearchActivity
import com.heyanle.easybangumi.anim.search.result.SearchResultFragment
import com.heyanle.easybangumi.databinding.FragmentAnimBinding
import com.heyanle.easybangumi.utils.getStringFromResource

/**
 * Created by HeYanLe on 2022/10/5 9:08.
 * https://github.com/heyanLE
 */
class AnimFragment: Fragment() {

    private lateinit var binding: FragmentAnimBinding
    private val titleList: List<String> by lazy {
        arrayListOf(
            R.string.anim_home.getStringFromResource(),
            R.string.anim_mine.getStringFromResource(),
        )
    }

    private val pageAdapter: PagerAdapter by lazy {
        PagerAdapter(
            requireActivity(),
            2
        ) {
            when (it) {
                0 -> AnimHomeFragment()
                else -> AnimMineFragment()
            }
        }
    }

    private val viewModel by activityViewModels<AnimViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnimBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabPageView()
        initToolbar()
    }

    private fun initTabPageView(){
        binding.viewPager.adapter = pageAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager, true, true
        ) { tab, po ->
            tab.text = titleList[po]
        }.attach()
        binding.viewPager.isNestedScrollingEnabled = true
    }

    private fun initToolbar(){
        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.item_search -> {
                    SearchActivity.start(this, viewModel.lastSelectSourceIndex)
                }
            }
            false
        }
    }

}