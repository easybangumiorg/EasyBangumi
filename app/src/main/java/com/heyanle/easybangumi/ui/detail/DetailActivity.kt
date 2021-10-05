package com.heyanle.easybangumi.ui.detail

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.adapter.PagerAdapter
import com.heyanle.easybangumi.databinding.ActivityDetailBinding
import com.heyanle.easybangumi.db.EasyDatabase
import com.heyanle.easybangumi.entity.Bangumi
import com.heyanle.easybangumi.entity.BangumiDetail
import com.heyanle.easybangumi.source.IBangumiDetailParser
import com.heyanle.easybangumi.source.ParserFactory
import com.heyanle.easybangumi.ui.BaseActivity
import com.heyanle.easybangumi.ui.detail.fragment.SourceFragment
import com.heyanle.easybangumi.ui.detail.viewmodel.DetailViewModel
import com.heyanle.easybangumi.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait

/**
 * Created by HeYanLe on 2021/9/21 13:41.
 * https://github.com/heyanLE
 */
class DetailActivity : BaseActivity() {

    companion object{

        private const val BANGUMI_KEY = "bangumi.key"

        fun start(activity: Activity, bangumi: Bangumi){
            val key = System.currentTimeMillis().toString()+activity.toString()
            GlobalUtils.put(key, bangumi)
            activity.start<DetailActivity> {
                putExtra(BANGUMI_KEY, key)
            }

        }
    }

    private val binding: ActivityDetailBinding by lazy {
        ActivityDetailBinding.inflate(LayoutInflater.from(this))
    }

    private lateinit var pagerAdapter: PagerAdapter

    private lateinit var bangumi: Bangumi

    private val viewModel by viewModels<DetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bangumi = GlobalUtils.get<Bangumi>(intent?.getStringExtra(BANGUMI_KEY)).let {
            if(it == null){
                finish()
                return
            }
            it
        }

        //window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT

        setContentView(binding.root)

        viewModel.bangumiDetail.observe(this){
            Glide.with(binding.cover).load(it.cover).into(binding.cover)
            Glide.with(binding.cover).load(it.cover).into(binding.backCover)

            binding.collapsing.title = it.name

            //binding.description.text = it.description
//            if(it.star){
//                binding.followBtn.setText(R.string.followed)
//                binding.followBtn.setBackgroundColor(getAttrColor(this, R.attr.subtitleTextColor))
//            }else{
//                binding.followBtn.setText(R.string.follow)
//                binding.followBtn.setBackgroundColor(getAttrColor(this, R.attr.colorSecondary))
//            }

        }
        binding.coverLayout.visible()
        viewModel.bangumiPlayMsg.observe(this){
            binding.coverLayout.visible()
            val keyList = arrayListOf<String>()
            it.iterator().let { ite ->
                while(ite.hasNext()){
                    keyList.add(ite.next().key)
                }
            }
            pagerAdapter = PagerAdapter(this, it.keys.size){ po ->
                SourceFragment(keyList[po])
            }
            binding.viewPager.adapter = pagerAdapter
            TabLayoutMediator(binding.tabLayout, binding.viewPager, true, true
            ) { tab, po ->
                tab.text = keyList[po]
            }.attach()
            binding.viewPager.visible()
        }

    }

    override fun onStart() {
        super.onStart()
        if(viewModel.bangumiDetail.value == null || viewModel.bangumiPlayMsg.value?.isEmpty() != false){
            refresh()
        }
    }

    private fun refresh(){
        GlobalScope.launch {
            binding.root.post {
                binding.progressBar.visible()
                binding.coverLayout.invisible()
                binding.viewPager.invisible()
            }

            val detailP = ParserFactory.detail(bangumi.source)?: return@launch
            val playP = ParserFactory.play(bangumi.source)?: return@launch
            val bangumiDetail: BangumiDetail? = detailP.detail(bangumi)?.let {
                EasyDatabase.AppDB.bangumiDetailDao().findBangumiDetailById(it.id).let {  list ->
                    if(list.isNotEmpty()){
                        val ban = list[0]
                        it.lastEpisodes = ban.lastEpisodes
                        it.lastVisitTime = ban.lastVisitTime
                        it.lastLine = ban.lastLine
                        it.lastProcessTime = ban.lastProcessTime
                        it.star = ban.star
                        EasyDatabase.AppDB.bangumiDetailDao().update(it)
                    }

                }
                viewModel.bangumiDetail.postValue(it)
                it
            }

            val map : LinkedHashMap<String, List<String>> = playP.getBangumiPlaySource(bangumi).let {
                viewModel.bangumiPlayMsg.postValue(it)
                it
            }
            if (bangumiDetail == null){
                error()
            }else{
                withContext(Dispatchers.Main){
                    runCatching {
                        binding.progressBar.invisible()
                        viewModel.playEpisode.value = Pair(map.keys.toList()[bangumiDetail.lastLine], bangumiDetail.lastEpisodes)
                    }.onFailure {
                        error()
                    }

                }
            }



        }




    }

    fun error(){}




}