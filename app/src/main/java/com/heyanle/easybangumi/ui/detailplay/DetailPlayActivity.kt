package com.heyanle.easybangumi.ui.detailplay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.heyanle.easybangumi.databinding.ActivityDetailPlayBinding
import com.heyanle.easybangumi.db.EasyDatabase
import com.heyanle.easybangumi.entity.Bangumi
import com.heyanle.easybangumi.entity.BangumiDetail
import com.heyanle.easybangumi.source.ParserFactory
import com.heyanle.easybangumi.ui.BaseActivity
import com.heyanle.easybangumi.ui.detailplay.adapter.EpisodeAdapter
import com.heyanle.easybangumi.ui.detailplay.adapter.PlayLineAdapter
import com.heyanle.easybangumi.ui.detailplay.viewmodel.DetailPlayViewModel
import com.heyanle.easybangumi.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import cn.jzvd.Jzvd
import com.heyanle.easybangumi.player.JZMediaExo
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import cn.jzvd.JZDataSource
import com.heyanle.easybangumi.R


/**
 * Created by HeYanLe on 2021/10/5 19:48.
 * https://github.com/heyanLE
 */
class DetailPlayActivity : BaseActivity() {

    companion object{

        private const val BANGUMI_KEY = "bangumi.key"

        fun start(activity: Context, bangumi: Bangumi){
            val key = System.currentTimeMillis().toString()+activity.toString()
            GlobalUtils.put(key, bangumi)
            activity.start<DetailPlayActivity> {
                putExtra(BANGUMI_KEY, key)
            }

        }
    }

    private val binding: ActivityDetailPlayBinding by lazy {
        ActivityDetailPlayBinding.inflate(LayoutInflater.from(this))
    }

    private lateinit var bangumi: Bangumi

    private val viewModel by viewModels<DetailPlayViewModel>()

    private val episodeTitleList = arrayListOf<String>()
    private val episodeAdapter: EpisodeAdapter by lazy {
        EpisodeAdapter(episodeTitleList)
    }

    private val playLineList = arrayListOf<String>()
    private val playLineAdapter: PlayLineAdapter by lazy {
        PlayLineAdapter(playLineList)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resources: Resources = this.resources
        val dm: DisplayMetrics = resources.displayMetrics
        val density = dm.density
        val width = dm.widthPixels
        binding.jzVideo.layoutParams.height = width/16*9

        setContentView(binding.root)

        binding.jzVideo.setMediaInterface(JZMediaExo::class.java)
        bangumi = GlobalUtils.get<Bangumi>(intent?.getStringExtra(BANGUMI_KEY)).let {
            if(it == null){
                finish()
                return
            }
            it
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        binding.recyclerEpisode.adapter = episodeAdapter
        binding.recyclerEpisode.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.recyclerPlayLine.adapter = playLineAdapter
        binding.recyclerPlayLine.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.jzVideo.onRetry = {
            refreshVideo()
            false
        }


        binding.btFollow.setOnClickListener { _ ->
            viewModel.bangumiDetail.value?.let {
                if (binding.btFollow.text.toString() == getString(R.string.follow)){
                    // 追番
                    it.star = true
                    it.lastVisiTime = System.currentTimeMillis()
                    EasyDatabase.AppDB.bangumiDetailDao().insert(it)
                }else{
                    // 取消
                    it.star = false
                    EasyDatabase.AppDB.bangumiDetailDao().delete(it)
                }
                viewModel.bangumiDetail.value = it
            }

        }

        binding.jzVideo.getCurrentIndex = {
            viewModel.realEpisode
        }

        binding.jzVideo.onEpisodeClick = {
            viewModel.playEpisode.value = it
            refreshVideo()
        }
        viewModel.bangumiDetail.observe(this){
            Glide.with(binding.cover).load(it.cover).into(binding.cover)
            binding.tvTitle.text = it.name
            binding.intro.text = "${ParserFactory.parser(it.source)!!.getLabel()}\n${it.description}"
            if(it.star){
                EasyDatabase.AppDB.bangumiDetailDao().update(it)
                binding.btFollow.setText(R.string.followed)
                binding.btFollow.setBackgroundColor(getAttrColor(this, R.attr.colorSurface))
                binding.btFollow.setTextColor(getAttrColor(this, R.attr.colorOnSurface))
            }else{
                binding.btFollow.setText(R.string.follow)
                binding.btFollow.setBackgroundColor(getAttrColor(this, R.attr.colorSecondary))
                binding.btFollow.setTextColor(getAttrColor(this, R.attr.colorOnSecondary))
            }

        }

        viewModel.bangumiPlayMsg.observe(this){
            episodeTitleList.clear()
            playLineList.clear()
            episodeAdapter.notifyDataSetChanged()
            playLineAdapter.notifyDataSetChanged()
            playLineList.addAll(it.keys)
        }
        viewModel.playPlayLine.observe(this){
            episodeTitleList.clear()
            episodeTitleList.addAll(viewModel.bangumiPlayMsg.value!![it]?: emptyList())
            playLineAdapter.selectTitle(it)
            episodeAdapter.notifyDataSetChanged()
            binding.episodeTv.text = getString(R.string.episode_total, episodeTitleList.size)
        }
        viewModel.playEpisode.observe(this){
            episodeAdapter.nowSelectIndex(it)
            refreshVideo()
        }
        playLineAdapter.onItemClickListener = {
            viewModel.playPlayLine.value = playLineList[it]
            val i = viewModel.playEpisode.value !!
            val ll = viewModel.bangumiPlayMsg.value!![playLineList[it]]!!
            if(i< 0 || i >= ll.size){
                if(ll.isEmpty()){
                    viewModel.playEpisode.value = -1
                }else
                    viewModel.playEpisode.value = 0
            }
            refreshVideo()
        }
        episodeAdapter.onItemClickListener = {
            viewModel.playEpisode.value = it
            refreshVideo()
        }
        binding.jzVideo.onSaveLast = { ti ->
            Log.i("DetailPlayActivity","onSaveLast")
            viewModel.bangumiDetail.value?.let {
                it.lastVisiTime = System.currentTimeMillis()
                it.lastEpisodes = viewModel.playEpisode.value!!
                it.lastEpisodeTitle = episodeTitleList[it.lastEpisodes]
                it.lastLine = playLineList.indexOf(viewModel.realPlayLine)
                it.lastProcessTime = ti
                Log.i("OnSave", it.toString())
                if(it.star){
                    EasyDatabase.AppDB.bangumiDetailDao().update(it)
                }

            }
        }
        refresh()
        binding.jzVideo.setUp("", "")
        binding.errorLayout.setOnClickListener {
            refresh()
        }
    }

    private fun refresh() {
        GlobalScope.launch {
            binding.root.post {
                binding.content.gone()
                binding.progressBar.visible()
                binding.errorLayout.gone()
            }
            val detailP = ParserFactory.detail(bangumi.source) ?: return@launch
            val playP = ParserFactory.play(bangumi.source) ?: return@launch
            val bangumiDetail: BangumiDetail? = detailP.detail(bangumi)?.let {
                EasyDatabase.AppDB.bangumiDetailDao().findBangumiDetailById(it.id).let { list ->
                    if (list.isNotEmpty()) {
                        val ban = list[0]
                        it.lastEpisodes = ban.lastEpisodes
                        it.lastLine = ban.lastLine
                        it.lastProcessTime = ban.lastProcessTime
                        it.star = ban.star
                        EasyDatabase.AppDB.bangumiDetailDao().update(it)
                    }

                }
                viewModel.bangumiDetail.postValue(it)
                it
            }
            val map: LinkedHashMap<String, List<String>> = playP.getBangumiPlaySource(bangumi).let {
                it.filter { entry ->
                    entry.value.isNotEmpty()
                }
                viewModel.bangumiPlayMsg.postValue(it)
                it
            }
            if (bangumiDetail == null || map.isEmpty()) {
                error()
            } else {
                withContext(Dispatchers.Main) {
                    runCatching {
                        binding.progressBar.gone()
                        binding.content.visible()
                        binding.errorLayout.gone()
                        if(bangumiDetail.lastLine < 0 || bangumiDetail.lastLine >= map.keys.size){
                            bangumiDetail.lastLine = 0
                        }
                        viewModel.playPlayLine.value = map.keys.toList()[bangumiDetail.lastLine]
                        bangumiDetail.lastEpisodes.let {
                            if(it >= 0 && it < map[viewModel.playPlayLine.value]?.size ?: Int.MAX_VALUE){
                                viewModel.playEpisode.value = bangumiDetail.lastEpisodes
                            }else{
                                viewModel.playEpisode.value = 0
                            }
                        }

                    }.onFailure {
                        it.printStackTrace()
                        error()
                    }

                }
            }
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
    private fun refreshVideo(){
        GlobalScope.launch {
            viewModel.bangumiDetail.value?.let {
                ParserFactory.play(it.source)?.let { parser ->
                    val playLine = viewModel.playPlayLine.value?:""
                    val playLineIndex = playLineList.indexOf(playLine)
                    val episode = viewModel.playEpisode.value ?: -1
                    binding.jzVideo.onSaveLast(0)
                    runCatching {
                        val url = parser.getBangumiPlayUrl(bangumi, playLineIndex, episode)
                        if(url.isEmpty()){
                            errorVideo()
                        }else{
                            runOnUiThread {
                                Log.i("DetailPlayActivity", url)
                                binding.jzVideo.episodeList.clear()
                                binding.jzVideo.episodeList.addAll(episodeTitleList)
                                viewModel.realPlayLine = playLineList[playLineIndex]
                                viewModel.realEpisode = episode

                                //binding.jzVideo.setUp(url , episodeTitleList[episode])
                                if(it.lastLine == playLineIndex && it.lastEpisodes == episode){
                                    binding.jzVideo.changeUrl(JZDataSource(url,episodeTitleList[episode]), it.lastProcessTime)
                                }else{
                                    binding.jzVideo.changeUrl(JZDataSource(url,episodeTitleList[episode]), 0)
                                }
                                binding.jzVideo.startVideo()
                            }
                        }
                    }.onFailure { th ->
                        th.printStackTrace()
                        errorVideo()
                    }

                }
            }
        }


    }
    private fun error(){
        runOnUiThread {
            binding.errorLayout.visible()
            binding.progressBar.gone()
            binding.content.gone()
        }
    }
    private fun errorVideo(){
        runOnUiThread {
            binding.jzVideo.changeUiToError()
        }
    }


    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        Jzvd.goOnPlayOnPause()
        Log.i("DetailPlayActivity","onPause")
        super.onPause()
        //Jzvd.releaseAllVideos()
    }

    override fun onDestroy() {
        Jzvd.releaseAllVideos()
        super.onDestroy()
    }

}