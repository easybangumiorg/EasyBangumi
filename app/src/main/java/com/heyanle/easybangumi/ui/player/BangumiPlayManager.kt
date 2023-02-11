package com.heyanle.easybangumi.ui.player

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.heyanle.easybangumi.BangumiApp
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.db.entity.BangumiHistory
import com.heyanle.easybangumi.player.PlayerController
import com.heyanle.easybangumi.player.PlayerTinyController
import com.heyanle.easybangumi.ui.common.easy_player.BaseEasyPlayerView
import com.heyanle.easybangumi.ui.common.easy_player.EasyPlayerView
import com.heyanle.easybangumi.ui.home.history.AnimHistoryViewModel
import com.heyanle.easybangumi.utils.toast
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import com.heyanle.eplayer_core.constant.EasyPlayerStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

/**
 * 1. 播放器引用（包括 页面 和 小窗）
 * 2. 维护当前 AnimPlayingController
 * 3. 连接 AnimPlayingController PlayController 和 当前播放器，将三者状态做联动
 * Created by HeYanLe on 2023/2/5 0:13.
 * https://github.com/heyanLE
 */
object BangumiPlayManager {

    data class EnterData(
        val lineIndex: Int = -1,
        val episode: Int = -1,
        val startProcess: Long = -1L,
    ) {}

    private val scope = MainScope()
    private var lastJob: Job? = null

    /**
     * 播放器 View 引用
     */
    private var composeViewRes: WeakReference<EasyPlayerView>? = null
    private var tinyViewRes: WeakReference<BaseEasyPlayerView>? = null

    /**
     * 当前播放 Controller
     */
    private var curAnimPlayingController: AnimPlayingController? = null

    /**
     * 指定初始线路，集数和进度
     */
    private val lastEnterData: EnterData?
        get() {
            return curAnimPlayingController?.enterData
        }

    /**
     * 上一个播放的 state ，防抖处理
     */
    private var lastPlayerStatus: AnimPlayState.Play? = null

    fun newAnimPlayItemController(controller: AnimPlayingController, enterData: EnterData?) {
        if (curAnimPlayingController != controller) {
            curAnimPlayingController = controller
            controller.enterData = enterData
            lastJob?.cancel()
            lastJob = scope.launch {
                controller.playerState.collectLatest {
                    newPlayerState(it)
                }
            }
        }

    }

    fun clearCurAnimPlayItemController() {
        curAnimPlayingController = null
    }

    fun getCurAnimPlayItemController(): AnimPlayingController? {
        return curAnimPlayingController
    }

    init {
        // 将播放器状态分发给 具体 的播放器 View
        PlayerController.playerControllerStatus.observeForever { state ->
            if (PlayerTinyController.isTinyMode) {
                this.tinyViewRes?.get()?.dispatchPlayStateChange(state)
            } else {
                this.composeViewRes?.get()?.basePlayerView?.dispatchPlayStateChange(state)
            }

        }
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

    fun onPlayerScreenReshow() {
        composeViewRes?.get()?.basePlayerView?.attachToPlayer(PlayerController.exoPlayer)
    }

    private fun newPlayerState(state: AnimPlayState) {
        when (state) {
            is AnimPlayState.Loading -> {
                if (PlayerTinyController.isTinyMode) {
                    PlayerTinyController.tinyPlayerView.basePlayerView.dispatchPlayStateChange(
                        EasyPlayStatus.STATE_PREPARING
                    )
                }
                PlayerController.exoPlayer.pause()
                PlayerController.exoPlayer.stop()
            }

            is AnimPlayState.Play -> {
                if (PlayerTinyController.isTinyMode) {
                    PlayerTinyController.tinyPlayerView.basePlayerView.refreshStateOnce()
                }
                // 如果播放器当前状态不在播放，则肯定要刷新播放源
                if (!PlayerController.isMedia() || lastPlayerStatus?.uri != state.uri || lastPlayerStatus?.type != state.type) {
                    val defaultDataSourceFactory =
                        DefaultDataSource.Factory(BangumiApp.INSTANCE)
                    val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                        BangumiApp.INSTANCE,
                        defaultDataSourceFactory
                    )
                    val media = when (state.type) {
                        C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(state.uri))

                        C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(
                                MediaItem.fromUri(state.uri)
                            )

                        else -> ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(
                                MediaItem.fromUri(state.uri)
                            )
                    }
                    val lastEnter = lastEnterData
                    if (lastEnter == null || lastEnter.lineIndex != state.lineIndex || lastEnter.episode != state.episode) {
                        PlayerController.setMediaSource(media, 0)
                    } else {
                        PlayerController.setMediaSource(media, lastEnter.startProcess)
                    }
                    curAnimPlayingController?.enterData = null
                    PlayerController.prepare()
                } else {
                    PlayerController.exoPlayer.seekTo(0)
                }
                lastPlayerStatus = state

            }

            is AnimPlayState.Error -> {
                if (PlayerTinyController.isTinyMode) {
                    state.errorMsg.toast()
                    PlayerTinyController.dismissTiny()
                    PlayerController.exoPlayer.stop()
                }
            }

            else -> {}
        }

    }

    fun trySaveHistory(ps: Long = -1) {
        var process = ps
        if (ps == -1L) {
            process =
                if (PlayerTinyController.isTinyMode)
                    tinyViewRes?.get()?.getCurrentPosition() ?: -1L
                else
                    composeViewRes?.get()?.basePlayerView?.getCurrentPosition() ?: -1L
        }
        if (process == -1L) {
            process = 0L
        }
        val curController = curAnimPlayingController ?: return
        // 只有在播放状态才更新历史
        if (!curController.isPlay()) {
            return
        }
        val infoState: BangumiInfoState.Info =
            (curController.infoState.value as? BangumiInfoState.Info) ?: return
        val playState: AnimPlayState.Play =
            (curController.playerState.value as? AnimPlayState.Play) ?: return
        scope.launch {
            withContext(Dispatchers.IO) {
                val lineTitle = kotlin.runCatching {
                    infoState.playMsg.keys.toList()[playState.lineIndex]
                }.getOrElse { "" }
                val episodeTitle = kotlin.runCatching {
                    infoState.playMsg[lineTitle]?.get(playState.episode) ?: ""
                }.getOrElse { "" }
                val history = BangumiHistory(
                    name = infoState.detail.name,
                    bangumiId = infoState.detail.id,
                    cover = infoState.detail.cover,
                    source = infoState.detail.source,
                    detailUrl = infoState.detail.detailUrl,
                    intro = infoState.detail.intro,
                    lastLinesIndex = playState.lineIndex,
                    lastLineTitle = lineTitle,
                    lastEpisodeIndex = playState.episode,
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

