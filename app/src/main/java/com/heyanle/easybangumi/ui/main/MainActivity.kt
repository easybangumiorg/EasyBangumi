package com.heyanle.easybangumi.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.widget.Toast
import com.google.android.material.tabs.TabLayoutMediator
import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ActivityMainBinding
import com.heyanle.easybangumi.ui.BaseActivity
import com.heyanle.easybangumi.adapter.PagerAdapter
import com.heyanle.easybangumi.ui.main.fragment.HomeFragment
import com.heyanle.easybangumi.ui.main.fragment.MyBangumiFragment
import com.heyanle.easybangumi.ui.main.fragment.SettingFragment
import com.heyanle.easybangumi.utils.DarkChangeSaveIntent
import com.heyanle.easybangumi.utils.DarkUtils

/**
 * Created by HeYanLe on 2021/9/19 10:28.
 * https://github.com/heyanLE
 */
class MainActivity : BaseActivity(), DarkChangeSaveIntent {

    companion object{
        const val INTENT_KEY_PAGER_INDEX = "INTENT.KEY.PAGER.INDEX"
    }

    private val titleList: List<String> by lazy {
        arrayListOf(
            EasyApplication.INSTANCE.getString(R.string.home),
            EasyApplication.INSTANCE.getString(R.string.my_bangumi),
            EasyApplication.INSTANCE.getString(R.string.setting),
        )
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(LayoutInflater.from(this))
    }

    private val pageAdapter: PagerAdapter by lazy {
        PagerAdapter(
            this,
            3
        ) {
            when (it) {
                0 -> HomeFragment()
                1 -> MyBangumiFragment()
                2 -> SettingFragment()
                else -> throw Exception("never run there, maybe")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //toolbarCenter(binding.toolbar)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.viewPager.adapter = pageAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager, true, true
        ) { tab, po ->
            tab.text = titleList[po]
        }.attach()
        val index = intent?.getIntExtra(INTENT_KEY_PAGER_INDEX, -1) ?: -1
        if(index != -1){
            binding.viewPager.setCurrentItem(index, false)
        }

        //binding.viewPager.isUserInputEnabled = false

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            menuInflater.inflate(R.menu.menu_main_activity_toolbar, it)
            val item = it.findItem(R.id.item_day_night)
            if (DarkUtils.dark()){
                item.setTitle(R.string.day_mode)
                item.setIcon(R.drawable.ic_baseline_wb_sunny_24)
            }else{
                item.setTitle(R.string.night_mode)
                item.setIcon(R.drawable.ic_baseline_brightness_2_24)
            }
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            it.findItem(R.id.item_day_night).setOnMenuItemClickListener {
                if(DarkUtils.autoDark()){
                    DarkUtils.autoDark(false, this)
                    Toast.makeText(this, R.string.channel_auto_day_night_mode, Toast.LENGTH_SHORT).show()
                }else{
                    DarkUtils.switch(this)
                }

                true
            }
        }
        return true
    }

    override fun getIntentDecorator(intent: Intent){
        intent.putExtra(INTENT_KEY_PAGER_INDEX, binding.viewPager.currentItem)
    }

}