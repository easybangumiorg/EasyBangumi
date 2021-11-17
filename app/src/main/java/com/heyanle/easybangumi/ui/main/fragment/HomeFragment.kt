package com.heyanle.easybangumi.ui.main.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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
import com.heyanle.easybangumi.source.ISourceParser
import com.heyanle.easybangumi.source.SourceParserFactory
import com.heyanle.easybangumi.ui.main.viewmodel.HomeFragmentViewModel
import com.heyanle.easybangumi.utils.getAttrColor
import com.heyanle.easybangumi.utils.gone
import com.heyanle.easybangumi.utils.oksp
import com.heyanle.easybangumi.utils.visible
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
            val keyList = SourceParserFactory.homeKeys()
            if(it < 0 || it >= keyList.size){
                binding.homeSource.text = getString(R.string.home_source_not_found)
                viewModel.bangumiMap.value?.let { map ->
                    map.clear()
                    viewModel.bangumiMap.value = map
                }
                return@observe
            }

            homeIndex = it
            binding.homeSource.text = getString(R.string.current_home_source, SourceParserFactory.homeKeys()[it])
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
            binding.refreshLayout.isRefreshing = false
            binding.contentLinear.removeAllViews()
            binding.errorLayout.gone()
            binding.contentLinear.visible()
            map.iterator().forEach {

                val bin = ItemHomeColumnBinding.inflate(layoutInflater, binding.contentLinear, true)
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
                    b.root.setOnClickListener {
                        SourceParserFactory.play(ba.source)?.startPlay(requireActivity(), ba)
                        //DetailPlayActivity.start(requireActivity(), ba)
                    }
                }
            }
            binding.refreshLayout.isRefreshing = false
        }
        viewModel.currentSourceIndex.value = homeIndex
        binding.sourceCard.setOnClickListener {
            showSourceDialog()
        }
        binding.errorLayout.setOnClickListener {
            refresh()
        }

    }


    override fun onResume() {
        super.onResume()
        //val j = viewModel.bangumiMap.value
        if(viewModel.bangumiMap.value == null || viewModel.bangumiMap.value!!.isEmpty()){
            if(binding.contentLinear.childCount == 0){
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
                binding.errorLayout.gone()
                binding.contentLinear.gone()
            }
            viewModel.currentSourceIndex.value?.let {
                val keyList = SourceParserFactory.homeKeys()
                if(it < 0 || it >= keyList.size){
                    binding.homeSource.text = getString(R.string.home_source_not_found)
                    viewModel.bangumiMap.value?.let { map ->
                        map.clear()
                        viewModel.bangumiMap.value = map
                    }
                }else{
                    when(val result = SourceParserFactory.home(keyList[it])?.home()){
                        is ISourceParser.ParserResult.Error -> {
                            if(result.isParserError){
                                kotlin.runCatching {
                                    Toast.makeText(EasyApplication.INSTANCE, R.string.home_source_error, Toast.LENGTH_SHORT ).show()
                                }

                            }
                            error()
                        }
                        is ISourceParser.ParserResult.Complete -> {
                            load(result.data)
                        }
                    }
                }

            }

        }
    }

    private fun load(map: LinkedHashMap<String, List<Bangumi>>){
        Log.i("HomeFragment", "load")
        viewModel.bangumiMap.postValue(map)
    }
    private fun error(){
        kotlin.runCatching {
            requireActivity().runOnUiThread {
                binding.errorLayout.visible()
                binding.contentLinear.gone()
                binding.refreshLayout.isRefreshing = false
            }
        }


    }
    private fun showSourceDialog(){
        kotlin.runCatching {
            val list = SourceParserFactory.homeKeys()
            val titles = Array(list.size){
                SourceParserFactory.home(list[it])?.getLabel()?:""
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




}