package com.heyanle.easybangumi.anim.home

import androidx.lifecycle.lifecycleScope
import com.heyanle.easy_daynight.ThemeManager
import com.heyanle.easybangumi.anim.AnimSourceFactory
import com.heyanle.easybangumi.anim.AnimViewModel
import com.heyanle.easybangumi.databinding.FragmentAnimHomeBinding
import com.heyanle.easybangumi.utils.gone
import com.heyanle.easybangumi.utils.visible

/**
 * Created by HeYanLe on 2022/9/18 15:48.
 * https://github.com/heyanLE
 */
class AnimHomeManager(
    private val fragment: AnimHomeFragment,
    private val viewModel: AnimHomeViewModel,
    private val parentViewModel: AnimViewModel,
    private val ui: FragmentAnimHomeBinding,
) {

    init {
        fragment.lifecycleScope.launchWhenResumed {
            viewModel.homeResultFlow.collect {
                handleState(it)
            }
        }
        initView()
    }

    private fun initView(){
        val homeTitle = arrayListOf<String>()
        val homeKeys = AnimSourceFactory.homeKeys()
        homeKeys.forEach {
            AnimSourceFactory.home(it)?.getLabel()?.apply {
                homeTitle.add(this)

            }
        }
        ui.sourceTabView.onTabClick = {
            viewModel.changeHomeSource(it)
            false
        }
        ui.errorLayout.isClickable = false
        ui.sourceTabView.setData(homeTitle)

        ui.refresh.setOnRefreshListener {
            viewModel.refresh()
        }

        ui.errorLayout.setOnClickListener {
            if(it.isClickable){
                viewModel.refresh()
            }
        }
        ui.refresh.setProgressBackgroundColorSchemeColor(ThemeManager.getAttrColor(fragment.requireContext(), android.R.attr.colorPrimary))
        ui.refresh.setColorSchemeColors(ThemeManager.getAttrColor(fragment.requireContext(), com.google.android.material.R.attr.colorSecondary))
    }

    private fun handleState(state: AnimHomeViewModel.HomeAnimState){
        when(state){
            is AnimHomeViewModel.HomeAnimState.Loading -> onLoading(state)
            is AnimHomeViewModel.HomeAnimState.Completely -> onCompletely(state)
            is AnimHomeViewModel.HomeAnimState.Error -> onError(state)
        }
    }

    private fun onLoading(state: AnimHomeViewModel.HomeAnimState.Loading){
        parentViewModel.lastSelectSourceIndex = state.curIndex
        ui.refresh.isClickable = false
        ui.errorLayout.gone()
        ui.animHomeView.gone()
        ui.sourceTabView.changeSelect(state.curIndex)
        ui.refresh.isRefreshing = true
    }

    private fun onCompletely(state: AnimHomeViewModel.HomeAnimState.Completely){
        parentViewModel.lastSelectSourceIndex = state.curIndex
        ui.sourceTabView.changeSelect(state.curIndex)
        ui.errorLayout.isClickable = false
        ui.errorLayout.gone()
        ui.refresh.isRefreshing = false
        ui.animHomeView.visible()
        ui.animHomeView.setData(state.data)
    }

    private fun onError(state: AnimHomeViewModel.HomeAnimState.Error){
        parentViewModel.lastSelectSourceIndex = state.curIndex
        ui.errorLayout.isClickable = true
        ui.errorLayout.visible()
        ui.animHomeView.gone()
        ui.sourceTabView.changeSelect(state.curIndex)
        ui.refresh.isRefreshing = false
    }

}