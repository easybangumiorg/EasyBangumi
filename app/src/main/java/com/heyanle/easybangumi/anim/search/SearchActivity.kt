package com.heyanle.easybangumi.anim.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.core.view.MenuItemCompat.getActionView
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.heyanle.easy_daynight.ThemeManager
import com.heyanle.easy_media.MediaHelper
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.adapter.PagerAdapter
import com.heyanle.easybangumi.anim.AnimSourceFactory
import com.heyanle.easybangumi.anim.home.AnimHomeFragment
import com.heyanle.easybangumi.anim.search.result.SearchResultFragment
import com.heyanle.easybangumi.anim.search.viewmodel.SearchViewModel
import com.heyanle.easybangumi.databinding.ActivityAnimSearchBinding

/**
 * Create by heyanlin on 2022/10/20
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

    private val viewModel by viewModels<SearchViewModel>()

    private var searchKeyList = arrayListOf<String>()

    private val pageAdapter: PagerAdapter by lazy {
        PagerAdapter(
            this,
            searchKeyList.size
        ) {
            //AnimHomeFragment()
            SearchResultFragment(searchKeyList[it], it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MediaHelper.setIsDecorFitsSystemWindows(this, true)
        MediaHelper.setStatusBarColor(this, ThemeManager.getAttrColor(this, androidx.appcompat.R.attr.colorPrimary))
        MediaHelper.setNavBarColor(this, ThemeManager.getAttrColor(this, android.R.attr.colorBackground))
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        searchKeyList.clear()
        searchKeyList.addAll(AnimSourceFactory.searchKeys())
        binding.viewPager.adapter = pageAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager, true, true
        ) { tab, po ->
            tab.text = AnimSourceFactory.search(searchKeyList[po])?.getLabel()?:""
        }.attach()

        val index = intent.getIntExtra(SearchActivity.KEY_DEF_KEYWORD_INDEX, 0)
        binding.viewPager.setCurrentItem(index, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        initSearchView(searchView = searchView)
        return super.onPrepareOptionsMenu(menu)
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

    private fun initSearchView(searchView: SearchView){
        searchView.isIconified = false
        searchView.setIconifiedByDefault(true)
        searchView.onActionViewExpanded()
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.submitKeyword(query?:"")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText == null || newText.isEmpty()){
                    viewModel.submitKeyword("")
                    return true
                }
                return false
            }
        })
    }

}