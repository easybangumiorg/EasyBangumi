package com.heyanle.easybangumi.ui.dlna

import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.easybangumi.ui.player.AnimPlayState
import com.heyanle.easybangumi.ui.player.AnimPlayingController
import com.heyanle.easybangumi.ui.player.BangumiPlayManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/11 21:15.
 * https://github.com/heyanLE
 */
object DlnaPlayingManager {

    data class EnterData(
        val lineIndex: Int = -1,
        val episode: Int = -1,
    ) {}

    val scope = MainScope()
    var lastJob: Job? = null

    var playingController = MutableStateFlow<AnimPlayingController?>(null)

    init {
        scope.launch {
            playingController.collectLatest {
                it?.let {
                    lastJob?.cancel()
                    lastJob = scope.launch {
                        it.playerState.collectLatest {
                            newPlayState(it)
                        }
                    }
                }
            }
        }
    }


    fun newBangumiSummary(bangumiSummary: BangumiSummary) {
        scope.launch {
            val old = playingController.value
            if (old == null || old.bangumiSummary != bangumiSummary) {
                old?.release()
                playingController.emit(AnimPlayingController(bangumiSummary))
            }
        }
    }

    private fun newPlayState(playState: AnimPlayState) {
        when (playState) {
            is AnimPlayState.Play -> {
                DlnaManager.playNew(playState.uri)
            }

            else -> {}
        }
    }

    fun refresh() {
        playingController.value?.playerState?.value?.let {
            newPlayState(it)
        }
    }

    fun release() {
        lastJob?.cancel()
        playingController.value = null
    }


}