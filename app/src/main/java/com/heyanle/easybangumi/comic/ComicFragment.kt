package com.heyanle.easybangumi.comic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.heyanle.easybangumi.databinding.FragmentAnimBinding
import com.heyanle.easybangumi.databinding.FragmentComicBinding

/**
 * Created by HeYanLe on 2022/10/5 9:08.
 * https://github.com/heyanLE
 */
class ComicFragment: Fragment() {

    private lateinit var binding: FragmentComicBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentComicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}