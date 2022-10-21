package com.heyanle.easybangumi.anim.search.result

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.heyanle.easy_daynight.ThemeManager
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.adapter.ItemLoadStateAdapter
import com.heyanle.easybangumi.anim.AnimSourceFactory
import com.heyanle.easybangumi.anim.search.viewmodel.SearchViewModel
import com.heyanle.easybangumi.anim.search.adapter.SearchBangumiAdapter
import com.heyanle.easybangumi.anim.search.paging.SearchPageSource
import com.heyanle.easybangumi.databinding.FragmentAnimSearchResultBinding
import com.heyanle.easybangumi.utils.gone
import com.heyanle.easybangumi.utils.visible
import com.heyanle.lib_anim.ISearchParser
import com.heyanle.lib_anim.entity.Bangumi
import kotlinx.coroutines.flow.collect

/**
 * Create by heyanlin on 2022/10/20
 */
class SearchResultFragment(): Fragment(R.layout.fragment_anim_search_result) {

    companion object{
        const val ARGUMENT_KEY_SOURCE_KEY = "ARGUMENT_KEY_SOURCE_KEY"
        const val ARGUMENT_KEY_INDEX = "ARGUMENT_KEY_INDEX"
    }

    constructor(key: String, index: Int):this(){
        arguments = Bundle().apply {
            putString(ARGUMENT_KEY_SOURCE_KEY, key)
            putInt(ARGUMENT_KEY_INDEX, index)
        }
    }

    lateinit var binding: FragmentAnimSearchResultBinding

    private val viewModel by activityViewModels<SearchViewModel> ()
    private lateinit var manager:SearchResultManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAnimSearchResultBinding.bind(view)
        initView()
    }

    private fun initView(){
        manager = SearchResultManager(this, viewModel, binding)
    }


}