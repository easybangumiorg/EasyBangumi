package com.heyanle.easybangumi.novel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.heyanle.easybangumi.databinding.FragmentComicBinding
import com.heyanle.easybangumi.databinding.FragmentNovelBinding

/**
 * Created by HeYanLe on 2022/10/5 9:08.
 * https://github.com/heyanLE
 */
class NovelFragment: Fragment() {

    private lateinit var binding: FragmentNovelBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNovelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}