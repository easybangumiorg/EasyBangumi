package com.heyanle.easybangumi.ui.search

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.adapter.PagerAdapter
import com.heyanle.easybangumi.databinding.ActivitySearchBinding
import com.heyanle.easybangumi.source.ParserFactory
import com.heyanle.easybangumi.ui.BaseActivity
import com.heyanle.easybangumi.ui.main.fragment.HomeFragment
import com.heyanle.easybangumi.ui.main.fragment.MyBangumiFragment
import com.heyanle.easybangumi.ui.main.fragment.SettingFragment
import com.heyanle.easybangumi.ui.search.fragment.SearchPageFragment
import com.heyanle.easybangumi.ui.search.viewmodel.SearchActivityViewModel
import com.heyanle.easybangumi.utils.DarkUtils

/**
 * Created by HeYanLe on 2021/10/9 13:03.
 * https://github.com/heyanLE
 */
class SearchActivity : BaseActivity() {

    val binding : ActivitySearchBinding by lazy {
        ActivitySearchBinding.inflate(LayoutInflater.from(this))
    }

    var searchKeyList = arrayListOf<String>()

    private val viewModel by viewModels<SearchActivityViewModel>()

    private val pageAdapter: PagerAdapter by lazy {
        PagerAdapter(
            this,
            searchKeyList.size
        ) {
            SearchPageFragment(searchKeyList[it], it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        searchKeyList.clear()
        searchKeyList.addAll(ParserFactory.searchKeys())
        binding.viewPager.adapter = pageAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager, true, true
        ) { tab, po ->
            tab.text = ParserFactory.search(searchKeyList[po])?.getLabel()?:""
        }.attach()

        binding.btSearch.setOnClickListener {
            viewModel.keyword.value = binding.etSearch.text.toString()
        }

        binding.etSearch.setOnKeyListener { _, keyCode, _ ->
            if(keyCode == KeyEvent.KEYCODE_ENTER){
                viewModel.keyword.value = binding.etSearch.text.toString()
                return@setOnKeyListener true
            }
            false
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}