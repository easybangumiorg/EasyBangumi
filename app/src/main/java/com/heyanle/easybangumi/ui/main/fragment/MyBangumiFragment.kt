package com.heyanle.easybangumi.ui.main.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.FragmentHomeBinding
import com.heyanle.easybangumi.databinding.FragmentMyFollowBinding
import com.heyanle.easybangumi.db.EasyDatabase
import com.heyanle.easybangumi.entity.BangumiDetail
import com.heyanle.easybangumi.source.ParserFactory
import com.heyanle.easybangumi.ui.main.adapter.ItemLoadStateAdapter
import com.heyanle.easybangumi.ui.main.adapter.MyBangumiAdapter
import com.heyanle.easybangumi.ui.main.viewmodel.MainActivityViewModel
import com.heyanle.easybangumi.utils.getAttrColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2021/9/20 15:55.
 * https://github.com/heyanLE
 */
class MyBangumiFragment : Fragment(R.layout.fragment_my_follow) {


    private val pager = Pager<Int, BangumiDetail>(
        PagingConfig(pageSize = 20)
    ){
        EasyDatabase.AppDB.bangumiDetailDao().findStarBangumiDetail()
    }
    private val adapter: MyBangumiAdapter by lazy {
        MyBangumiAdapter()
    }
    private val concatAdapter: ConcatAdapter by lazy {
        adapter.withLoadStateHeaderAndFooter(
            header = ItemLoadStateAdapter(adapter::retry),
            footer = ItemLoadStateAdapter(adapter::retry)
        )
    }

    private lateinit var binding: FragmentMyFollowBinding

    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyFollowBinding.bind(view)

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = concatAdapter

        lifecycleScope.launch(Dispatchers.IO){
            pager.flow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        adapter.onItemClickListener = { _, d ->
            d.getBangumi().let {
                ParserFactory.play(it.source)?.startPlayActivity(requireActivity(), it)
            }

        }

        adapter.onItemMoreListener = {i, d ->
            showBangumiMenuDialog(i, d)
        }

        adapter.addLoadStateListener {
            binding.refreshLayout.isRefreshing = it.refresh is LoadState.Loading || it.refresh is LoadState.Loading
        }
        binding.refreshLayout.setOnRefreshListener {
            adapter.refresh()
        }

        binding.refreshLayout.setColorSchemeColors(getAttrColor(requireContext(), R.attr.colorSecondary))


    }

    private fun showBangumiMenuDialog(position: Int, bangumiDetail: BangumiDetail){
        MaterialAlertDialogBuilder(requireContext()).apply{
            setItems(R.array.my_bangumi_menu_array){_,i ->
                when(i){
                    0 -> {
                        Toast.makeText(EasyApplication.INSTANCE, R.string.delete_follow_bangumi_completely, Toast.LENGTH_SHORT).show()
                        bangumiDetail.star = false
                        EasyDatabase.AppDB.bangumiDetailDao().delete(bangumiDetail)
                        adapter.refresh()
                    }
                }
            }
            show()
        }
    }


    override fun onResume() {
        super.onResume()
        Log.i("MyBangumiFragment", "onResume")
        if(activityViewModel.refreshMyBangumi.value == true){
            Log.i("MyBangumiFragment", "refresh")
            adapter.refresh()
            adapter.notifyDataSetChanged()
            activityViewModel.refreshMyBangumi.value = false
        }
    }



}