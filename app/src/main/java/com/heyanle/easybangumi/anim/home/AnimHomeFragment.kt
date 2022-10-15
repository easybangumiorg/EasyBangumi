package com.heyanle.easybangumi.anim.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.heyanle.easybangumi.anim.AnimViewModel
import com.heyanle.easybangumi.databinding.FragmentAnimHomeBinding

/**
 * Created by HeYanLe on 2022/10/5 9:31.
 * https://github.com/heyanLE
 */
class AnimHomeFragment: Fragment() {

    private lateinit var binding: FragmentAnimHomeBinding
    private val viewModel by viewModels<AnimHomeViewModel>()
    private val parentViewModel by activityViewModels<AnimViewModel>()
    private lateinit var manager: AnimHomeManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnimHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){
        manager = AnimHomeManager(this, viewModel, parentViewModel, binding)
    }

}