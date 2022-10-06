package com.heyanle.easybangumi.anim.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.heyanle.easybangumi.databinding.FragmentAnimMineBinding

/**
 * Created by HeYanLe on 2022/10/5 9:32.
 * https://github.com/heyanLE
 */
class AnimMineFragment: Fragment() {

    private lateinit var binding: FragmentAnimMineBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnimMineBinding.inflate(inflater, container, false)
        return binding.root
    }

}