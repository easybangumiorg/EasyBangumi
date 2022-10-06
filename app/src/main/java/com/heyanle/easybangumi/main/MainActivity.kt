package com.heyanle.easybangumi.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.heyanle.easy_media.MediaHelper
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.adapter.PagerAdapter
import com.heyanle.easybangumi.anim.AnimFragment
import com.heyanle.easybangumi.comic.ComicFragment
import com.heyanle.easybangumi.databinding.ActivityMainBinding
import com.heyanle.easybangumi.novel.NovelFragment
import com.heyanle.easybangumi.setting.SettingFragment

/**
 * Created by HeYanLe on 2022/9/28 16:02.
 * https://github.com/heyanLE
 */
class MainActivity : AppCompatActivity() {

    private val bottomNavItemId = arrayListOf(R.id.menu_bangumi, R.id.menu_comic, R.id.menu_novel, R.id.menu_setting)
    private val pageAdapter: PagerAdapter by lazy {
        PagerAdapter(
            this,
            3
        ) {
            when (it) {
                0 -> AnimFragment()
                1 -> ComicFragment()
                2 -> NovelFragment()
                3 -> SettingFragment()
                else -> throw Exception("never run there, maybe")
            }
        }
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(LayoutInflater.from(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        MediaHelper.setIsFitSystemWindows(binding.vp2Main, true)
        binding.vp2Main.adapter = pageAdapter
        binding.vp2Main.isUserInputEnabled = false
        binding.bottomNav.setOnItemSelectedListener {
            binding.vp2Main.setCurrentItem(bottomNavItemId.indexOf(it.itemId), false)
            true
        }
    }
}