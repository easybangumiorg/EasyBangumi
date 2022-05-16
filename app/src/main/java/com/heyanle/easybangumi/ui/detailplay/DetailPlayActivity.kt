package com.heyanle.easybangumi.ui.detailplay

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import com.bumptech.glide.Glide
import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ActivityDetailPlayBinding
import com.heyanle.easybangumi.db.EasyDatabase
import com.heyanle.easybangumi.entity.Bangumi
import com.heyanle.easybangumi.player.JZMediaExo
import com.heyanle.easybangumi.source.IDetailParser
import com.heyanle.easybangumi.source.IPlayerParser
import com.heyanle.easybangumi.source.SourceParserFactory
import com.heyanle.easybangumi.ui.BaseActivity
import com.heyanle.easybangumi.ui.detailplay.adapter.EpisodeAdapter
import com.heyanle.easybangumi.ui.detailplay.adapter.PlayLineAdapter
import com.heyanle.easybangumi.ui.detailplay.viewmodel.DetailPlayViewModel
import com.heyanle.easybangumi.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.ref.WeakReference


/**
 * Created by HeYanLe on 2021/10/5 19:48.
 * https://github.com/heyanLE
 */
class DetailPlayActivity : BaseActivity() {

    companion object{

        const val HANDLER_REFRESH = 0
        const val HANDLER_LOAD_DETAIL = 1
        const val HANDLER_LOAD_PLAY_MSG = 2
        const val HANDLER_PREPARE_PLAY = 3
        const val HANDLER_PLAY = 4
        const val HANDLER_ERROR = 5

        private const val BANGUMI_KEY = "bangumi.key"

        fun start(activity: Context, bangumi: Bangumi){
            val key = System.currentTimeMillis().toString()+activity.toString()
            GlobalUtils.put(key, bangumi)
            activity.start<DetailPlayActivity> {
                putExtra(BANGUMI_KEY, key)
            }

        }
    }

    class DetailHandler(
        detailPlayActivity: DetailPlayActivity
    ): Handler(Looper.getMainLooper()){

        private val weakActivity = WeakReference(detailPlayActivity)

        init {
            detailPlayActivity.lifecycle.addObserver(object: LifecycleObserver{
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy(){
                    weakActivity.clear()
                    detailPlayActivity.lifecycle.removeObserver(this)
                    removeCallbacksAndMessages(null)
                }
            })
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val detailPlayActivity = weakActivity.get()?:return
            when(msg.what){
                HANDLER_REFRESH -> {
                    detailPlayActivity.performRefresh()
                }
                HANDLER_LOAD_DETAIL -> {
                    detailPlayActivity.performLoadDetail()
                }
                HANDLER_LOAD_PLAY_MSG -> {
                    detailPlayActivity.performLoadPlayMsg()
                }
                HANDLER_ERROR -> {
                    detailPlayActivity.performError()
                }
                HANDLER_PREPARE_PLAY -> {
                    detailPlayActivity.performPreparePlay()
                }
                HANDLER_PLAY -> {
                    detailPlayActivity.performPlay()
                }
            }
        }
    }

    private val handler: DetailHandler by lazy {
        DetailHandler(this)
    }

    private val binding: ActivityDetailPlayBinding by lazy {
        ActivityDetailPlayBinding.inflate(LayoutInflater.from(this))
    }

    private lateinit var bangumi: Bangumi
    private lateinit var playParser: IPlayerParser
    private lateinit var detailParser: IDetailParser

    private val viewModel by viewModels<DetailPlayViewModel>()

    private val episodeTitleList = arrayListOf<String>()
    private val episodeAdapter: EpisodeAdapter by lazy {
        EpisodeAdapter(episodeTitleList)
    }

    private val playLineList = arrayListOf<String>()
    private val playLineAdapter: PlayLineAdapter by lazy {
        PlayLineAdapter(playLineList)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resources: Resources = this.resources
        val dm: DisplayMetrics = resources.displayMetrics
        dm.density
        val width = dm.widthPixels
        val height = dm.heightPixels
        binding.jzVideo.layoutParams.height = (width / 16 * 9).coerceAtMost(height / 3)

        setContentView(binding.root)


        binding.jzVideo.setMediaInterface(JZMediaExo::class.java)
        bangumi = GlobalUtils.get<Bangumi>(intent?.getStringExtra(BANGUMI_KEY)).let {
            if(it == null){
                finish()
                return
            }
            it
        }
        playParser = SourceParserFactory.play(bangumi.source).let {
            if(it == null){
                finish()
                return
            }else{
                it
            }
        }
        detailParser = SourceParserFactory.detail(bangumi.source).let {
            if(it == null){
                finish()
                return
            }else{
                it
            }
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        binding.recyclerEpisode.adapter = episodeAdapter
        binding.recyclerEpisode.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.recyclerPlayLine.adapter = playLineAdapter
        binding.recyclerPlayLine.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        viewModel.bangumiDetail.observe(this){
            episodeTitleList.clear()
            playLineList.clear()
            episodeAdapter.notifyDataSetChanged()
            playLineAdapter.notifyDataSetChanged()
            Glide.with(binding.cover).load(it.cover).into(binding.cover)
            binding.tvTitle.text = it.name
            binding.intro.text = "${playParser.getLabel()}\n${it.description}"
            if(it.star){
                binding.btFollow.setText(R.string.followed)
                binding.btFollow.setBackgroundColor(getAttrColor(this, R.attr.colorSurface))
                binding.btFollow.setTextColor(getAttrColor(this, R.attr.colorOnSurface))
            }else{
                binding.btFollow.setText(R.string.follow)
                binding.btFollow.setBackgroundColor(getAttrColor(this, R.attr.colorSecondary))
                binding.btFollow.setTextColor(getAttrColor(this, R.attr.colorOnSecondary))
            }
        }
        viewModel.playMsg.observe(this){
            episodeTitleList.clear()
            playLineList.clear()
            playLineList.addAll(it.keys)

            val list = it.keys.toList()
            if(list.isEmpty()){
                episodeAdapter.notifyDataSetChanged()
                playLineAdapter.notifyDataSetChanged()
                return@observe
            }
            if(viewModel.nowPlayLineIndex < 0 || viewModel.nowPlayLineIndex >= list.size){
                viewModel.nowPlayLineIndex = 0
            }
            val epList = it[list[viewModel.nowPlayLineIndex]]?: emptyList()
            if(epList.isEmpty()){
                episodeAdapter.notifyDataSetChanged()
                playLineAdapter.notifyDataSetChanged()
                return@observe
            }
            if(viewModel.nowPlayEpisode < 0 || viewModel.nowPlayEpisode >= epList.size){
                viewModel.nowPlayEpisode = 0
            }


            playLineAdapter.selectTitle = list[viewModel.nowPlayLineIndex]
            episodeTitleList.clear()
            episodeTitleList.addAll(epList)
            binding.jzVideo.episodeList.clear()
            binding.jzVideo.episodeList.addAll(episodeTitleList)
            episodeAdapter.nowSelectIndex = viewModel.nowPlayEpisode
            episodeAdapter.notifyDataSetChanged()
            playLineAdapter.notifyDataSetChanged()

            binding.episodeTv.text = getString(R.string.episode_total, episodeTitleList.size)


        }

        episodeAdapter.onItemClickListener = {
            viewModel.nowPlayEpisode = it
            episodeAdapter.nowSelectIndex(it)
            preparePlay()
        }
        playLineAdapter.onItemClickListener = {
            viewModel.nowPlayLineIndex = it
            playLineAdapter.selectTitle(playLineList[it])
            viewModel.nowPlayEpisode = 0

            episodeTitleList.clear()
            val list = (viewModel.playMsg.value?: linkedMapOf()).keys.toList()

            if(list.isEmpty()){

                episodeTitleList.clear()
                binding.jzVideo.episodeList.clear()
                episodeAdapter.notifyDataSetChanged()
                preparePlay()
            }else{
                episodeTitleList.clear()
                episodeTitleList.addAll(viewModel.playMsg.value?.get(list[it])?: emptyList())
                binding.jzVideo.episodeList.clear()
                binding.jzVideo.episodeList.addAll(episodeTitleList)
                episodeAdapter.notifyDataSetChanged()
                preparePlay()
            }
            binding.episodeTv.text = getString(R.string.episode_total, episodeTitleList.size)

        }

        binding.jzVideo.setUp("", "")
        binding.jzVideo.getCurrentIndex = {
            viewModel.nowPlayEpisode
        }
        binding.jzVideo.onEpisodeClick = {
            viewModel.nowPlayEpisode = it
            episodeAdapter.nowSelectIndex(it)
            preparePlay()
        }
        binding.jzVideo.onRetry = {
            preparePlay()
            false
        }
        binding.btFollow.setOnClickListener {
            val detail = viewModel.bangumiDetail.value.let {
                if(it == null){
                    refresh()
                    return@setOnClickListener
                }else{
                    it
                }
            }
            if(detail.star){
                detail.star = false
                binding.btFollow.setText(R.string.follow)
                binding.btFollow.setBackgroundColor(getAttrColor(this, R.attr.colorSecondary))
                binding.btFollow.setTextColor(getAttrColor(this, R.attr.colorOnSecondary))
                EasyDatabase.AppDB.bangumiDetailDao().delete(detail)
            }else{
                detail.star = true
                EasyDatabase.AppDB.bangumiDetailDao().insert(detail)
                EasyDatabase.AppDB.bangumiDetailDao().update(detail)
                runCatching {
                    detail.lastLine = viewModel.nowPlayLineIndex
                    detail.lastEpisodes = viewModel.nowPlayEpisode
                    detail.lastEpisodeTitle = episodeTitleList[viewModel.nowPlayEpisode]
                }
                binding.btFollow.setText(R.string.followed)
                binding.btFollow.setBackgroundColor(getAttrColor(this, R.attr.colorSurface))
                binding.btFollow.setTextColor(getAttrColor(this, R.attr.colorOnSurface))
            }
        }
        binding.jzVideo.onSaveLast = { ti ->
            Log.i("DetailPlayActivity","onSaveLast")
            viewModel.bangumiDetail.value?.let {
                it.lastVisiTime = System.currentTimeMillis()
                it.lastEpisodes = viewModel.nowPlayEpisode
                it.lastEpisodeTitle = kotlin.runCatching { episodeTitleList[it.lastEpisodes] }.getOrNull()?:""
                it.lastLine = viewModel.nowPlayLineIndex
                it.lastProcessTime = ti
                Log.i("OnSave", it.toString())
                if(it.star){
                    EasyDatabase.AppDB.bangumiDetailDao().update(it)
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

    private fun refresh(){
        Message.obtain(handler, HANDLER_REFRESH).sendToTarget()
    }
    private fun loadDetail(){
        Message.obtain(handler, HANDLER_LOAD_DETAIL).sendToTarget()
    }
    private fun loadPlayMsg(){
        Message.obtain(handler, HANDLER_LOAD_PLAY_MSG).sendToTarget()
    }
    private fun play(){
        Message.obtain(handler, HANDLER_PLAY).sendToTarget()
    }
    private fun error(){
        Message.obtain(handler, HANDLER_ERROR).sendToTarget()
    }
    private fun preparePlay(){
        Message.obtain(handler, HANDLER_PREPARE_PLAY).sendToTarget()
    }

    private fun performRefresh(){
        handler.removeCallbacksAndMessages(null)
        loadingUi()
        loadDetail()
    }
    private fun performLoadDetail(){
        loadingUi()
        GlobalScope.launch {
            detailParser.detail(bangumi).complete {
                val detail = it.data
                viewModel.bangumiDetail.postValue(it.data)

                EasyDatabase.AppDB.bangumiDetailDao().findBangumiDetailById(detail.id).forEach { b ->
                    detail.lastProcessTime = b.lastProcessTime
                    detail.lastLine = b.lastLine
                    detail.lastEpisodes = b.lastEpisodes
                    detail.star = b.star
                    detail.lastVisiTime = System.currentTimeMillis()
                }
                viewModel.nowPlayLineIndex = detail.lastLine
                viewModel.nowPlayEpisode = detail.lastEpisodes
                if(detail.star){
                    EasyDatabase.AppDB.bangumiDetailDao().update(detail)
                }
                loadPlayMsg()

            }.error {
                if (it.isParserError){
                    runOnUiThread {
                        Toast.makeText(EasyApplication.INSTANCE, R.string.source_error, Toast.LENGTH_SHORT).show()
                    }
                }
                it.throwable.printStackTrace()
                error()
            }
        }

    }
    private fun performLoadPlayMsg(){
        loadingUi()
        GlobalScope.launch {
            playParser.getPlayMsg(bangumi).complete {
                viewModel.playMsg.postValue(it.data)
                runOnUiThread {
                    contentUi()
                }
                val list = it.data.keys.toList()
                viewModel.playUrl = Array(it.data.size){ po ->
                    val li = it.data[list[po]]?: emptyList()
                    Array(li.size){""}
                }
                preparePlay()
            }.error {
                if (it.isParserError){
                    runOnUiThread {
                        Toast.makeText(EasyApplication.INSTANCE, R.string.source_error, Toast.LENGTH_SHORT).show()
                    }
                }
                it.throwable.printStackTrace()
                error()
            }
        }
    }
    private fun performPreparePlay(){
        binding.jzVideo.changeUiToPreparing()
        contentUi()
        //binding.jzVideo.changeUiToPreparing()
        val playMsg = viewModel.playMsg.value.let {
            if(it == null){
                //handler.removeCallbacksAndMessages(null)
                errorVideo()
                return
            }else{
                it
            }
        }
        if(playMsg.isEmpty()){
            Jzvd.releaseAllVideos()
            return
        }
        val playLineIndex = viewModel.nowPlayLineIndex
        val playEpisode = viewModel.nowPlayEpisode

        if(playLineIndex < 0 ||playLineIndex >= playMsg.size){
            //handler.removeCallbacksAndMessages(null)
            errorVideo()
            return
        }
        val playLine = playMsg.keys.toList()[playLineIndex]
        val list = playMsg[playLine]!!
        if(list.isEmpty()){
            Jzvd.goOnPlayOnPause()
            return
        }
        if(playEpisode < 0 || playEpisode >= list.size){
            errorVideo()
            return
        }
        try{
            val url = viewModel.playUrl[playLineIndex][playEpisode]
            if(url.isNotEmpty()){
                play()
                return
            }

        }catch (e : Exception){
            e.printStackTrace()
        }

        GlobalScope.launch {
            playParser.getPlayUrl(bangumi, playLineIndex, playEpisode)
                .complete {
                    if(it.data == ""){
                        errorVideo()
                    }else{
                        withContext(Dispatchers.Main){
                            viewModel.playUrl[playLineIndex][playEpisode] = it.data
                            play()
                        }
                    }
                }.error {
                    if(it.isParserError){
                        runOnUiThread {
                            Toast.makeText(EasyApplication.INSTANCE, R.string.source_error, Toast.LENGTH_SHORT).show()
                        }
                    }
                    it.throwable.printStackTrace()
                    errorVideo()
                }
        }
    }
    private fun performPlay(){
        val playLine = viewModel.nowPlayLineIndex
        val playEpisode = viewModel.nowPlayEpisode
        val bangumiDetail = viewModel.bangumiDetail.value.let {
            if(it == null){
                errorVideo()
                return
            }else{
                it
            }
        }
        val playMsg = viewModel.playMsg.value.let {
            if(it == null){
                errorVideo()
                return
            }else{
                it
            }
        }
        runCatching {
            val url = viewModel.playUrl[playLine][playEpisode]
            //url.logEWithDebug("DetailPlayActivity")
            if(url != ""){
                val title = episodeTitleList[playEpisode]
                var device = 0L
                if(bangumiDetail.lastLine == playLine
                    && bangumiDetail.lastEpisodes == playEpisode){
                    device = bangumiDetail.lastProcessTime
                }
                binding.jzVideo.setMediaInterface(JZMediaExo::class.java)
                binding.jzVideo.changeUrl(JZDataSource(url, title), device)
                binding.jzVideo.startVideo()
            }
        }.onFailure {
            viewModel.playUrl = emptyArray()
            errorVideo()
            it.printStackTrace()
        }

    }
    private fun performError(){
        handler.removeCallbacksAndMessages(null)
        errorUi()
    }

    private fun contentUi(){
        binding.progressBar.gone()
        binding.errorLayout.gone()
        binding.content.visible()
    }
    private fun loadingUi(){
        binding.progressBar.visible()
        binding.errorLayout.gone()
        binding.content.invisible()
    }
    private fun errorUi(){
        binding.errorLayout.visible()
        binding.progressBar.gone()
        binding.content.gone()
    }
    private fun errorVideo(){

        runOnUiThread {
            Jzvd.goOnPlayOnPause()
            binding.jzVideo.changeUiToError()
        }
    }


    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        if(viewModel.playMsg.value == null
            || viewModel.bangumiDetail.value == null){
            refresh()
        }else if(binding.jzVideo.state == Jzvd.STATE_IDLE){
            preparePlay()
        }
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