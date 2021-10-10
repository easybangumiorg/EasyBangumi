package com.heyanle.easybangumi.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ActivitySearchBinding
import com.heyanle.easybangumi.ui.BaseActivity
import com.heyanle.easybangumi.utils.DarkUtils

/**
 * Created by HeYanLe on 2021/10/9 13:03.
 * https://github.com/heyanLE
 */
class SearchActivity : BaseActivity() {

    val binding : ActivitySearchBinding by lazy {
        ActivitySearchBinding.inflate(LayoutInflater.from(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            menuInflater.inflate(R.menu.menu_search_activity_toolbar, it)

        }
        return true
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