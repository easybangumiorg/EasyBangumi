package com.heyanle.easybangumi.ui.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.collection.LruCache
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.easybangumi.BangumiApp
import com.heyanle.easybangumi.MainActivity
import com.heyanle.easybangumi.NAV
import com.heyanle.easybangumi.PLAY
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.db.entity.BangumiHistory
import com.heyanle.easybangumi.player.PlayerController
import com.heyanle.easybangumi.player.PlayerTinyController
import com.heyanle.easybangumi.player.TinyStatusController
import com.heyanle.easybangumi.ui.common.easy_player.BaseEasyPlayerView
import com.heyanle.easybangumi.ui.common.easy_player.EasyPlayerView
import com.heyanle.easybangumi.ui.home.history.AnimHistoryViewModel
import com.heyanle.easybangumi.utils.toast
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import com.heyanle.eplayer_core.constant.EasyPlayerStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.lang.ref.WeakReference
import java.net.URLEncoder

/**
 * Created by HeYanLe on 2023/1/15 19:53.
 * https://github.com/heyanLE
 */
object BangumiPlayController {

    data class EnterData(
        val sourceIndex: Int = -1,
        val episode: Int = -1,
        val startProcess: Long = -1L,
    ) {}

    private val scope = MainScope()
    private var lastScope = MainScope()

    private var lastProgress = -1L

    val lastPauseLevel = mutableStateOf(0)
    private var composeViewRes: WeakReference<EasyPlayerView>? = null

    private var tinyViewRes: WeakReference<BaseEasyPlayerView>? = null

    private val animPlayViewModelCache = ItemLru()

    val curAnimPlayViewModel = MutableLiveData<AnimPlayItemController>()

    val curPlayerStatus = MutableLiveData<AnimPlayItemController.PlayerStatus>()

    var pendingIntent: PendingIntent? = null

    fun getCurPendingIntent(): PendingIntent {
        return if (pendingIntent != null) {
            pendingIntent!!
        } else {
            val intent = Intent(BangumiApp.INSTANCE, MainActivity::class.java)
            val flagImmutable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
            PendingIntent.getActivity(BangumiApp.INSTANCE, 0, intent, flagImmutable)
        }
    }

    fun newBangumi(bangumiSummary: BangumiSummary, enterData: EnterData? = null) {

        val new = getAnimPlayViewModel(bangumiSummary, enterData)
        if (new != curAnimPlayViewModel.value) {
            curAnimPlayViewModel.postValue(new)
        }
        new.onNewEnter(enterData)
        lastProgress = enterData?.startProcess ?: -1L
        val del = URLEncoder.encode(bangumiSummary.detailUrl, "utf-8")

        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            "${NAV}://${PLAY}/${bangumiSummary.source}/${del}".toUri(),
            BangumiApp.INSTANCE,
            MainActivity::class.java
        )

        pendingIntent = TaskStackBuilder.create(BangumiApp.INSTANCE).run {
            addNextIntentWithParentStack(deepLinkIntent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }


    }

    fun getAnimPlayViewModel(
        bangumiSummary: BangumiSummary,
        enterData: EnterData? = null
    ): AnimPlayItemController {
        val cache = animPlayViewModelCache[bangumiSummary]
        if (cache == null) {
            val n = AnimPlayItemController(bangumiSummary, enterData)
            animPlayViewModelCache.put(bangumiSummary, n)
            return n
        }
        return cache
    }

    fun onNewComposeView(easyPlayerView: EasyPlayerView) {
        if (easyPlayerView != this.composeViewRes?.get()) {
            this.composeViewRes = WeakReference(easyPlayerView)
        }
        PlayerController.playerControllerStatus.value?.let {
            val playerState =
                if (easyPlayerView.basePlayerView.isFullScreen()) EasyPlayerStatus.PLAYER_FULL_SCREEN else EasyPlayerStatus.PLAYER_NORMAL
            easyPlayerView.basePlayerView.dispatchPlayerStateChange(playerState)
            easyPlayerView.basePlayerView.dispatchPlayStateChange(it)
        }

    }

    fun onNewTinyComposeView(easyPlayerView: BaseEasyPlayerView) {
        if (easyPlayerView != this.tinyViewRes?.get()) {
            this.tinyViewRes = WeakReference(easyPlayerView)
        }
        PlayerController.playerControllerStatus.value?.let {
            easyPlayerView.dispatchPlayerStateChange(EasyPlayerStatus.PLAYER_TINY_SCREEN)
            easyPlayerView.dispatchPlayStateChange(it)
        }
    }

    // activity 的 onPause 调用
    fun onPause() {
        lastPauseLevel.value = lastPauseLevel.value + 1
    }

    fun onPlayerScreenReshow() {
        composeViewRes?.get()?.basePlayerView?.attachToPlayer(PlayerController.exoPlayer)
    }

    var lastPlayerStatus: AnimPlayItemController.PlayerStatus.Play? = null

    fun trySaveHistory(ps: Long = -1) {
        var process = ps
        if (ps == -1L) {
            process = composeViewRes?.get()?.basePlayerView?.getCurrentPosition() ?: -1L
        }
        val playVM = curAnimPlayViewModel.value ?: return
        val ds = playVM.detailController.detailFlow.value
        val ms = playVM.playMsgController.flow.value
        val ps = playVM.playerStatus.value
        Log.d(
            "BangumiPlayController",
            "trySave ${ds.javaClass.simpleName} ${ms.javaClass.simpleName} ${ps.javaClass.simpleName} ${ps.sourceIndex} ${ps.episode} $process"
        )
        if (
            ds is DetailController.DetailStatus.Completely &&
            ms is PlayMsgController.PlayMsgStatus.Completely &&
            ps is AnimPlayItemController.PlayerStatus.Play
        ) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val lineTitle = kotlin.runCatching {
                        ms.playMsg.keys.toList()[ps.sourceIndex]
                    }.getOrElse { "" }
                    val episodeTitle = kotlin.runCatching {
                        ms.playMsg[lineTitle]?.get(ps.episode) ?: ""
                    }.getOrElse { "" }
                    val history = BangumiHistory(
                        name = ds.bangumiDetail.name,
                        cover = ds.bangumiDetail.cover,
                        source = ds.bangumiDetail.source,
                        detailUrl = ds.bangumiDetail.detailUrl,
                        intro = ds.bangumiDetail.intro,
                        lastLinesIndex = ps.sourceIndex,
                        lastLineTitle = lineTitle,
                        lastEpisodeIndex = ps.episode,
                        lastEpisodeTitle = episodeTitle,
                        lastProcessTime = process,
                        createTime = System.currentTimeMillis()
                    )
                    EasyDB.database.bangumiHistory.insertOrModify(history)
                    AnimHistoryViewModel.refresh()
                }
            }
        }
    }

    init {
        PlayerController.playerControllerStatus.observeForever { state ->
            if (PlayerTinyController.isTinyMode) {
                this.tinyViewRes?.get()?.dispatchPlayStateChange(state)
            } else {
                this.composeViewRes?.get()?.basePlayerView?.dispatchPlayStateChange(state)
            }

        }
        curAnimPlayViewModel.observeForever {
            lastScope.cancel()
            val scope = MainScope()
            lastScope = scope
            scope.launch {
                it.playerStatus.collectLatest {
                    curPlayerStatus.postValue(it)
                }
            }
        }
        curPlayerStatus.observeForever {
            scope.launch {
                when (it) {
                    is AnimPlayItemController.PlayerStatus.Loading -> {
                        if (PlayerTinyController.isTinyMode) {
                            PlayerTinyController.tinyPlayerView.basePlayerView.dispatchPlayStateChange(
                                EasyPlayStatus.STATE_PREPARING
                            )
                        }
                    }

                    is AnimPlayItemController.PlayerStatus.Play -> {
                        Log.d("BangumiPlayController", "onPlay $lastProgress")
                        Log.d("BangumiPlayController", it.uri)
                        val vm = curAnimPlayViewModel.value ?: return@launch
                        if (PlayerTinyController.isTinyMode) {
                            //PlayerTinyController.tinyPlayerView.basePlayerView.refreshStateOnce()
                        }
                        if (lastPlayerStatus?.uri != it.uri || lastPlayerStatus?.type != it.type) {
                            val defaultDataSourceFactory =
                                DefaultDataSource.Factory(BangumiApp.INSTANCE)
                            val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                                BangumiApp.INSTANCE,
                                defaultDataSourceFactory
                            )
                            val media = when (it.type) {
                                C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
                                    .createMediaSource(MediaItem.fromUri(it.uri))

                                C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
                                    .createMediaSource(
                                        MediaItem.fromUri(it.uri)
                                    )

                                else -> ProgressiveMediaSource.Factory(dataSourceFactory)
                                    .createMediaSource(
                                        MediaItem.fromUri(it.uri)
                                    )
                            }

                            if (lastProgress > 0) {
                                PlayerController.setMediaSource(media, lastProgress)
                            } else {
                                PlayerController.setMediaSource(media, 0)
                            }
                            lastProgress = -1
                            PlayerController.prepare()
                        } else {
                            PlayerController.exoPlayer.seekTo(0)
                        }
                        lastPlayerStatus = it

                    }

                    is AnimPlayItemController.PlayerStatus.Error -> {
                        if (PlayerTinyController.isTinyMode) {
                            it.errorMsg.toast()
                            PlayerTinyController.dismissTiny()
                            PlayerController.exoPlayer.stop()
                        }

                    }

                    else -> {
                    }
                }
            }

        }
    }

    class ItemLru : LruCache<BangumiSummary, AnimPlayItemController>(3) {
        override fun entryRemoved(
            evicted: Boolean,
            key: BangumiSummary,
            oldValue: AnimPlayItemController,
            newValue: AnimPlayItemController?
        ) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            kotlin.runCatching {
                oldValue.clear()
            }.onFailure {
                it.printStackTrace()
            }

        }
    }

}