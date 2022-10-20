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

    private val adapter: SearchBangumiAdapter by lazy {
        SearchBangumiAdapter()
    }
    private val concatAdapter: ConcatAdapter by lazy {
        adapter.withLoadStateHeaderAndFooter(
            header = ItemLoadStateAdapter(adapter::retry),
            footer = ItemLoadStateAdapter(adapter::retry)
        )
    }

    private val sourceKey : String
        get() {
            return requireArguments().getString(ARGUMENT_KEY_SOURCE_KEY, "")?:""
        }
    private val searchParser: ISearchParser by lazy {
        AnimSourceFactory.search(sourceKey)?:throw NullPointerException()
    }

    private val viewModel by activityViewModels<SearchViewModel> ()

    private val pager: Pager<Int, Bangumi> by lazy {
        Pager(
            PagingConfig(pageSize = 10)
        ){
            SearchPageSource(searchParser, viewModel.keywordFlow.value)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAnimSearchResultBinding.bind(view)
        init()
    }

//    private var lastKeyWord = ""
//
//    override fun onResume() {
//        super.onResume()
//        if(lastKeyWord != (viewModel.keywordFlow.value) || adapter.itemCount == 0){
//            refresh(viewModel.keywordFlow.value)
//        }
//    }

    private fun init(){
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = concatAdapter

        binding.refreshLayout.isClickable = false

        adapter.addLoadStateListener {
            binding.refreshLayout.isRefreshing = it.refresh is LoadState.Loading
            if(it.refresh is LoadState.NotLoading){
                binding.recycler.visible()
            }
            if(it.refresh is LoadState.Error && viewModel.keywordFlow.value.isNotEmpty()){
                (it.refresh as LoadState.Error).error.printStackTrace()
                binding.recycler.gone()
                binding.emptyLayout.gone()
                binding.errorLayout.visible()
            }
        }

        binding.refreshLayout.setOnRefreshListener {
            refresh(viewModel.keywordFlow.value)
        }
        binding.errorLayout.setOnClickListener {
            refresh(viewModel.keywordFlow.value)
            binding.refreshLayout.isRefreshing = true
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            pager.flow.collect {
                adapter.submitData(it)
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.keywordFlow.collect{
                refresh(it)
            }
        }

        binding.root.isNestedScrollingEnabled = true
        binding.refreshLayout.isNestedScrollingEnabled = true
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(ThemeManager.getAttrColor(requireContext(), android.R.attr.colorPrimary))
        binding.refreshLayout.setColorSchemeColors(ThemeManager.getAttrColor(requireContext(), com.google.android.material.R.attr.colorSecondary))
        binding.recycler.gone()
        binding.emptyLayout.visible()
        binding.errorLayout.gone()
    }

    private fun refresh(keyword: String){
        if(keyword.isEmpty()){
            binding.recycler.gone()
            binding.emptyLayout.visible()
            binding.errorLayout.gone()
            binding.refreshLayout.isRefreshing = false
            return
        }else{
            binding.refreshLayout.isRefreshing = true
            binding.recycler.gone()
            binding.emptyLayout.gone()
            binding.errorLayout.gone()
            adapter.refresh()
        }
    }

}