package com.heyanle.easybangumi.ui.player

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.heyanle.bangumi_source_api.api.entity.BangumiDetail
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.player.PlayerController
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.utils.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 维护一部番的相关播放页数据，包括
 * 1. 番剧信息（BangumiDetailed 和 PlayMsg）
 * 2. 播放状态
 *
 * Created by HeYanLe on 2023/2/4 23:28.
 * https://github.com/heyanLE
 */
class AnimPlayingController(
    val bangumiSummary: BangumiSummary,
) {

    private var scope = MainScope()
    private var lastJob: Job? = null

    private val infoController = BangumiInfoController(bangumiSummary)

    private val _playerState = MutableStateFlow<AnimPlayState>(
        AnimPlayState.None
    )
    val playerState: StateFlow<AnimPlayState> = _playerState

    val infoState: StateFlow<BangumiInfoState> = infoController.flow

    val isBangumiStar = infoController.isBangumiStar


    fun loadInfo() {
        lastJob?.cancel()
        lastJob = null
        scope.launch {
            _playerState.emit(AnimPlayState.None)
        }
        infoController.load()
    }

    fun loadPlay(
        lineIndex: Int,
        episodeIndex: Int,
    ) {
        Log.d("AnimPlayingController", "loadPlay ${lineIndex} ${episodeIndex}")
        val info = infoController.flow.value
        if (info !is BangumiInfoState.Info) {
            return
        }
        if (lineIndex < 0 || lineIndex >= info.playMsg.size) {
            return
        }
        val lines = info.playMsg.toList()[lineIndex]
        if (episodeIndex < 0 || episodeIndex >= lines.second.size) {
            return
        }
        lastJob?.cancel()
        val job = scope.launch {
            _playerState.emit(
                AnimPlayState.Loading(
                    lineIndex,
                    episodeIndex,
                )
            )
            PlayerController.exoPlayer.pause()
            PlayerController.exoPlayer.stop()
            kotlin.runCatching {
                val res = AnimSourceFactory.requirePlay(bangumiSummary.source)
                val result = res.getPlayUrl(bangumiSummary, lineIndex, episodeIndex)
                result.complete {
                    _playerState.emit(
                        AnimPlayState.Play(
                            lineIndex,
                            episodeIndex,
                            it.data.uri,
                            it.data.type,
                        )
                    )
                }.error {
                    it.throwable.printStackTrace()
                    _playerState.emit(
                        AnimPlayState.Error(
                            lineIndex,
                            episodeIndex,
                            if (it.isParserError) stringRes(
                                R.string.source_error
                            ) else stringRes(R.string.loading_error), it.throwable
                        )
                    )
                }
            }.onFailure {
                if (isActive) {
                    it.printStackTrace()
                    _playerState.emit(
                        AnimPlayState.Error(
                            lineIndex,
                            episodeIndex,
                            stringRes(R.string.loading_error),
                            it
                        )
                    )
                }

            }
        }
        lastJob = job
    }

    fun setBangumiStar(isStar: Boolean, bangumiDetail: BangumiDetail) {
        infoController.setBangumiStar(isStar, bangumiDetail)
    }

    /**
     * 这里返回的是番剧播放状态机是否在播放
     * 不代表真正的播放器在播放（播放器可能是暂停和缓存状态）
     */
    fun isPlay(): Boolean {
        val infoState = infoController.flow.value
        return infoState is BangumiInfoState.Info
                && playerState.value is AnimPlayState.Play
    }

    fun onShow(enterData: BangumiPlayManager.EnterData?) {

        scope.launch {
            val curPlayState = playerState.value
            Log.d("AnimPlayingController", "${enterData} ${curPlayState}")
            when (curPlayState) {
                is AnimPlayState.None -> {
                    _playerState.emit(
                        AnimPlayState.Loading(
                            0,
                            0,
                        )
                    )
                    val newEnterData = getRealEnterDataWhenFirst(enterData)
                    BangumiPlayManager.newEnterData(newEnterData)
                    loadPlay(newEnterData.lineIndex, newEnterData.episode)
                }

                is AnimPlayState.Loading -> {
                    val newEnterData = getRealEnterDataWhenFirst(enterData)
                    BangumiPlayManager.newEnterData(newEnterData)
                    loadPlay(newEnterData.lineIndex, newEnterData.episode)
                }

                is AnimPlayState.Play -> {
                    // 如果当前已经在播放，则必须显示指定 enterData 才触发刷新
                    if (enterData != null && enterData.lineIndex >= 0 && enterData.episode >= 0) {
                        BangumiPlayManager.newEnterData(enterData)
                        loadPlay(enterData.lineIndex, enterData.episode)
                    }
                }

                else -> {

                }
            }
        }

    }


    private suspend fun getRealEnterDataWhenFirst(
        enterData: BangumiPlayManager.EnterData?
    ): BangumiPlayManager.EnterData {
        if (enterData == null || enterData.lineIndex == -1) {
            return getEnterDataFromHistory()
        }
        return enterData
    }

    private suspend fun getEnterDataFromHistory(): BangumiPlayManager.EnterData {
        return withContext(Dispatchers.IO) {
            val hist = EasyDB.database.bangumiHistory.getFromBangumiSummary(
                bangumiSummary.source,
                bangumiSummary.detailUrl
            )
            if (hist == null || hist.lastLinesIndex == -1) {
                return@withContext BangumiPlayManager.EnterData(0, 0, 0)
            }
            return@withContext BangumiPlayManager.EnterData(
                hist.lastLinesIndex,
                hist.lastEpisodeIndex.coerceAtLeast(0),
                hist.lastProcessTime.coerceAtLeast(0)
            )
        }
    }

    fun release() {
        infoController.release()
        lastJob?.cancel()
        scope.cancel()
        lastJob = null
    }


}

sealed class AnimPlayState(
    val lineIndex: Int,
    val episode: Int,
) {
    object None : AnimPlayState(-1, -1)

    class Loading(
        sourceIndex: Int,
        episode: Int,
    ) : AnimPlayState(sourceIndex, episode)

    class Play(
        sourceIndex: Int,
        episode: Int,
        val uri: String,
        val type: Int = 0
    ) : AnimPlayState(sourceIndex, episode)

    class Error(
        sourceIndex: Int,
        episode: Int,
        val errorMsg: String,
        val throwable: Throwable?
    ) : AnimPlayState(sourceIndex, episode)
}

