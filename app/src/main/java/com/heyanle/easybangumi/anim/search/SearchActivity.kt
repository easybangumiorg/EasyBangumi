package com.heyanle.easybangumi.anim.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.heyanle.easy_daynight.ThemeManager
import com.heyanle.easy_media.MediaHelper
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.adapter.PagerAdapter
import com.heyanle.easybangumi.anim.AnimSourceFactory
import com.heyanle.easybangumi.anim.home.AnimHomeFragment
import com.heyanle.easybangumi.anim.search.page.SearchPageFragment
import com.heyanle.easybangumi.anim.search.viewmodel.SearchActivityViewModel
import com.heyanle.easybangumi.databinding.ActivityAnimSearchBinding
import com.heyanle.easybangumi.databinding.ActivityMainBinding

/**
 * Created by HeYanLe on 2022/10/6 9:30.
 * https://github.com/heyanLE
 */
class SearchActivity: AppCompatActivity() {

    companion object {
        private const val KEY_DEF_KEYWORD_INDEX = "KEY_DEF_KEYWORD_INDEX"

        fun start(activity: Activity, keywordIndex: Int = 0){
            val intent = Intent(activity, SearchActivity::class.java)
            intent.putExtra(KEY_DEF_KEYWORD_INDEX, keywordIndex)
            activity.startActivity(intent)
        }

        fun start(fragment: Fragment, keywordIndex: Int = 0){
            val intent = Intent(fragment.requireContext(), SearchActivity::class.java)
            intent.putExtra(KEY_DEF_KEYWORD_INDEX, keywordIndex)
            fragment.startActivity(intent)
        }

    }

    private val binding: ActivityAnimSearchBinding by lazy {
        ActivityAnimSearchBinding.inflate(LayoutInflater.from(this))
    }

    private var searchKeyList = arrayListOf<String>()

    private val viewModel by viewModels<SearchActivityViewModel>()

    private val pageAdapter: PagerAdapter by lazy {
        PagerAdapter(
            this,
            searchKeyList.size
        ) {
            AnimHomeFragment()
            // SearchPageFragment(searchKeyList[it], it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //MediaHelper.setIsFitSystemWindows(binding.root, true)
        MediaHelper.setIsDecorFitsSystemWindows(this, false)
        MediaHelper.setStatusBarColor(this, ThemeManager.getAttrColor(this, androidx.appcompat.R.attr.colorPrimary))
        MediaHelper.setNavBarColor(this, ThemeManager.getAttrColor(this, androidx.appcompat.R.attr.colorPrimary))

        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        searchKeyList.clear()
        searchKeyList.addAll(AnimSourceFactory.searchKeys())
        binding.viewPager.adapter = pageAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager, true, true
        ) { tab, po ->
            tab.text = AnimSourceFactory.search(searchKeyList[po])?.getLabel()?:""
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

        val index = intent.getIntExtra(KEY_DEF_KEYWORD_INDEX, 0)
        binding.viewPager.setCurrentItem(index, false)
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