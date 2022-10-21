package com.heyanle.easybangumi.anim.search.result

import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.heyanle.easy_daynight.ThemeManager
import com.heyanle.easybangumi.adapter.ItemLoadStateAdapter
import com.heyanle.easybangumi.anim.AnimSourceFactory
import com.heyanle.easybangumi.anim.search.adapter.SearchBangumiAdapter
import com.heyanle.easybangumi.anim.search.viewmodel.SearchViewModel
import com.heyanle.easybangumi.databinding.FragmentAnimSearchResultBinding
import com.heyanle.easybangumi.utils.gone
import com.heyanle.easybangumi.utils.visible
import com.heyanle.lib_anim.ISearchParser
import com.heyanle.lib_anim.entity.Bangumi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okhttp3.internal.readFieldOrNull

/**
 * Create by heyanlin on 2022/10/21
 */
class SearchResultManager(
    private val fragment: SearchResultFragment,
    private val viewModel: SearchViewModel,
    private val binding: FragmentAnimSearchResultBinding,
) {

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
            return fragment.requireArguments().getString(SearchResultFragment.ARGUMENT_KEY_SOURCE_KEY, "")?:""
        }

    private val searchParser: ISearchParser by lazy {
        AnimSourceFactory.search(sourceKey)?:throw NullPointerException()
    }

    private val pager: Pager<Int, Bangumi> by lazy {
        viewModel.getPager(searchParser)
    }



    init {

        fragment.lifecycleScope.launchWhenResumed {
            pager.flow.collect {
                adapter.submitData(fragment.lifecycle, it)
            }
        }

        fragment.lifecycleScope.launchWhenResumed {
            adapter.loadStateFlow.map {
                it.refresh
            }.distinctUntilChanged().collect{
                if(it is LoadState.Loading){
                    performLoading()
                    return@collect
                }
                if(viewModel.keywordLiveData.value.isNullOrEmpty()){
                    performEmpty()
                    return@collect
                }

                if(it is LoadState.NotLoading){
                    performCompletely()
                    return@collect
                }

                if(it is LoadState.Error){
                    (it as LoadState.Error).error.printStackTrace()
                    performError()
                    return@collect
                }
            }
        }
        viewModel.keywordLiveData.observe(fragment.viewLifecycleOwner) {
            if(it.isEmpty()){
                performEmpty()
            }else{
                adapter.refresh()
            }
        }
        initView()
    }

    private fun initView(){
        binding.recycler.layoutManager = LinearLayoutManager(fragment.requireContext())
        binding.recycler.adapter = concatAdapter
        binding.recycler.isNestedScrollingEnabled = true
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(ThemeManager.getAttrColor(fragment.requireContext(), android.R.attr.colorPrimary))
        binding.refreshLayout.setColorSchemeColors(ThemeManager.getAttrColor(fragment.requireContext(), com.google.android.material.R.attr.colorSecondary))
        binding.refreshLayout.setOnRefreshListener {
            if(viewModel.keywordLiveData.value.isNullOrEmpty()){
                performEmpty()
            }else{
                adapter.refresh()
            }
        }
        binding.emptyLayout.setOnClickListener {
            if(viewModel.keywordLiveData.value.isNullOrEmpty()){
                performEmpty()
            }else{
                adapter.refresh()
            }
        }
        binding.errorLayout.setOnClickListener {
            if(viewModel.keywordLiveData.value.isNullOrEmpty()){
                performEmpty()
            }else{
                adapter.refresh()
            }
        }
    }

    private fun performLoading(){
        binding.refreshLayout.isRefreshing = true
        binding.refreshLayout.visible()
        binding.recycler.scrollTo(0,0 )
        binding.recycler.gone()
        binding.errorLayout.gone()
        binding.emptyLayout.gone()
    }

    private fun performEmpty(){
        binding.refreshLayout.isRefreshing = false
        binding.refreshLayout.gone()
        binding.recycler.gone()
        binding.errorLayout.gone()
        binding.emptyLayout.visible()
    }

    private fun performCompletely(){
        binding.refreshLayout.isRefreshing = false
        binding.refreshLayout.visible()
        binding.recycler.visible()
        binding.errorLayout.gone()
        binding.emptyLayout.gone()
    }

    private fun performError(){
        binding.refreshLayout.isRefreshing = false
        binding.refreshLayout.gone()
        binding.errorLayout.visible()
        binding.recycler.gone()
        binding.emptyLayout.gone()
    }

}