package com.heyanle.easybangumi.ui.main.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.FragmentHomeBinding
import com.heyanle.easybangumi.databinding.ItemHomeColumnBangumiBinding
import com.heyanle.easybangumi.databinding.ItemHomeColumnBinding
import com.heyanle.easybangumi.entity.Bangumi
import com.heyanle.easybangumi.source.ParserFactory
import com.heyanle.easybangumi.ui.detail.DetailActivity
import com.heyanle.easybangumi.ui.main.viewmodel.HomeFragmentViewModel
import com.heyanle.easybangumi.utils.getAttrColor
import com.heyanle.easybangumi.utils.oksp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2021/9/20 15:55.
 * https://github.com/heyanLE
 */
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    private val viewModel by viewModels<HomeFragmentViewModel> ()

    @Volatile private var isChangeSource = false

    private var homeIndex by oksp("Home_Index", 0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        viewModel.currentSourceIndex.observe(viewLifecycleOwner){
            homeIndex = it
            binding.homeSource.text = getString(R.string.current_home_source, ParserFactory.homeKeys()[it])
            if(isChangeSource){
                refresh()
                isChangeSource = false
            }
        }
        binding.refreshLayout.setColorSchemeColors(getAttrColor(requireContext(), R.attr.colorSecondary))
        binding.refreshLayout.setOnRefreshListener {
            refresh()
        }
        viewModel.bangumiMap.observe(viewLifecycleOwner){ map ->
            binding.refreshLayout.isRefreshing = true
            binding.linear.removeAllViews()
            binding.linear.addView(binding.sourceCard)
            map.iterator().forEach {
                val bin = ItemHomeColumnBinding.inflate(layoutInflater, binding.linear, true)
                bin.columnTitle.text = it.key
                it.value.forEach { ba ->
                    val b = ItemHomeColumnBangumiBinding.inflate(layoutInflater, bin.linear, true)
                    Glide.with(b.cover).load(ba.cover).into(b.cover)
                    b.title.text = ba.name
                    b.intro.text = ba.intro
//                    b.root.setOnClickListener {
//                        DetailActivity.start(requireActivity(), ba)
//                    }
                    b.root.onTouchDown = {
                        bin.scroll.homeColumnLinearLayout = b.root
                    }
                    b.root.onTouchUpWithoutMove = {
                        DetailActivity.start(requireActivity(), ba)
                    }
                }
            }
            binding.refreshLayout.isRefreshing = false
        }
        viewModel.currentSourceIndex.value = homeIndex
        binding.sourceCard.setOnClickListener {
            showSourceDialog()
        }
    }


    override fun onResume() {
        super.onResume()
        val j = viewModel.bangumiMap.value
        if(viewModel.bangumiMap.value == null || viewModel.bangumiMap.value!!.isEmpty()){
            if(binding.linear.childCount <= 1){
                refresh()
            }
        }

    }

    private fun refresh(){
        Log.i(viewModel.toString(), "refresh")
        Log.i(this.toString(), "refresh")
        GlobalScope.launch {
            binding.refreshLayout.post {
                binding.refreshLayout.isRefreshing = true
                binding.linear.removeAllViews()
                binding.linear.addView(binding.sourceCard)
            }
            viewModel.currentSourceIndex.value?.let {
                val map = ParserFactory.home(ParserFactory.homeKeys()[it])?.home()?:return@let
                load(map)
            }
        }
    }

    private fun load(map: LinkedHashMap<String, List<Bangumi>>){
        viewModel.bangumiMap.postValue(map)
    }

    private fun showSourceDialog(){
        val list = ParserFactory.homeKeys()
        val titles = Array<String>(list.size){
            ParserFactory.home(list[it])?.getLabel()?:""
        }
        MaterialAlertDialogBuilder(requireContext()).apply {
            setItems(titles) { _, i ->
                if(i == homeIndex){
                    return@setItems
                }else{
                    isChangeSource = true
                    viewModel.currentSourceIndex.value = i
                }
            }
            setTitle(R.string.change_home_source)
        }.show()
    }




}