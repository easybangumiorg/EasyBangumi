package com.heyanle.easybangumi.anim.search.page

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.adapter.ItemLoadStateAdapter
import com.heyanle.easybangumi.anim.AnimSourceFactory
import com.heyanle.easybangumi.anim.search.adapter.SearchBangumiAdapter
import com.heyanle.easybangumi.anim.search.paging.SearchPageSource
import com.heyanle.easybangumi.anim.search.viewmodel.SearchActivityViewModel
import com.heyanle.easybangumi.databinding.FragmentAnimSearchPageBinding
import com.heyanle.easybangumi.utils.gone
import com.heyanle.easybangumi.utils.visible
import com.heyanle.lib_anim.ISearchParser
import com.heyanle.lib_anim.entity.Bangumi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2021/10/10 19:42.
 * https://github.com/heyanLE
 */
class SearchPageFragment(): Fragment(R.layout.fragment_anim_search_page){

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

    lateinit var binding: FragmentAnimSearchPageBinding

    private val sourceKey : String
        get() {
            return requireArguments().getString(ARGUMENT_KEY_SOURCE_KEY, "")?:""
        }
    private val searchParser: ISearchParser by lazy {
        AnimSourceFactory.search(sourceKey)?:throw NullPointerException()
    }

    private val pager: Pager<Int, Bangumi> by lazy {
        Pager(
            PagingConfig(pageSize = 10)
        ){
            SearchPageSource(searchParser, activityViewModel.keyword.value?:"")
        }
    }

    private val activityViewModel by activityViewModels<SearchActivityViewModel>()

    var lastKeyWord = ""



    private val adapter: SearchBangumiAdapter by lazy {
        SearchBangumiAdapter()
    }
    private val concatAdapter: ConcatAdapter by lazy {
        adapter.withLoadStateHeaderAndFooter(
            header = ItemLoadStateAdapter(adapter::retry),
            footer = ItemLoadStateAdapter(adapter::retry)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kotlin.runCatching {
            searchParser
        }.onFailure {
            it.printStackTrace()
            error()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAnimSearchPageBinding.bind(view)

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = concatAdapter
        // binding.recycler.isNestedScrollingEnabled = false

        activityViewModel.keyword.observe(viewLifecycleOwner){
            if(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                refresh(it)
            }

        }

        adapter.addLoadStateListener {
            binding.refreshLayout.isRefreshing = it.refresh is LoadState.Loading
        }

        binding.refreshLayout.setOnRefreshListener {
            refresh(activityViewModel.keyword.value?:"")
        }
        binding.errorLayout.setOnClickListener {
            refresh(activityViewModel.keyword.value?:"")
            binding.refreshLayout.isRefreshing = true
        }

        lifecycleScope.launchWhenResumed {
            pager.flow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        adapter.onItemClickListener = { _, ba->
            //AnimSourceFactory.play(ba.source)?.startPlay(requireActivity(), ba)
        }
        binding.refreshLayout.isNestedScrollingEnabled = true
        binding.recycler.isNestedScrollingEnabled = true
    }

    override fun onResume() {
        super.onResume()
        if(lastKeyWord != (activityViewModel.keyword.value ?: "") || adapter.itemCount == 0){
            refresh(activityViewModel.keyword.value?:"")
        }
    }


    private fun error(){
        runCatching {
            requireActivity().runOnUiThread{
                binding.recycler.gone()
                binding.emptyLayout.gone()
                binding.errorLayout.visible()
            }
        }
    }

    private fun refresh(keyword: String){
        if(keyword.isEmpty()){
            binding.recycler.gone()
            binding.emptyLayout.visible()
            binding.errorLayout.gone()
            binding.refreshLayout.isRefreshing = false
            return
        }else{
            binding.refreshLayout.isRefreshing = false
            binding.recycler.visible()
            binding.emptyLayout.gone()
            binding.errorLayout.gone()
            lastKeyWord = keyword
            adapter.refresh()
        }
    }




}