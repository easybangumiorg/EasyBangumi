package com.heyanle.easybangumi.ui.detail.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.FragmentRecyclerBinding
import com.heyanle.easybangumi.ui.detail.adapter.EpisodeAdapter
import com.heyanle.easybangumi.ui.detail.viewmodel.DetailViewModel
import com.google.android.flexbox.JustifyContent

import com.google.android.flexbox.FlexDirection

import com.google.android.flexbox.FlexboxLayoutManager




/**
 * Created by HeYanLe on 2021/9/21 14:37.
 * https://github.com/heyanLE
 */
class SourceFragment (): Fragment(R.layout.fragment_recycler) {

    companion object{
        const val ARGUMENTS_KEY = "sourceKey"
    }

    constructor(sourceKey: String):this(){
        arguments = Bundle().apply {
            putString(ARGUMENTS_KEY, sourceKey)
        }
    }

    private val sourceKey: String
    get() {
        return arguments?.getString(ARGUMENTS_KEY)?:""
    }

    private lateinit var binding: FragmentRecyclerBinding

    private val activityViewModel by activityViewModels<DetailViewModel>()

    val list = arrayListOf<String>()
    private val adapter: EpisodeAdapter by lazy {
        EpisodeAdapter(list)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecyclerBinding.bind(view)

        binding.recycler.adapter = adapter
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        binding.recycler.layoutManager = layoutManager



        activityViewModel.playEpisode.observe(viewLifecycleOwner){
            if (it.first == sourceKey){
                adapter.lastIndex = it.second
                adapter.notifyDataSetChanged()
            }
        }

        activityViewModel.bangumiPlayMsg.observe(viewLifecycleOwner){ map ->
            map[sourceKey]?.let {
                list.clear()
                list.addAll(it)
                adapter.notifyDataSetChanged()
            }
        }
    }

}