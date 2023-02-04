package com.heyanle.easybangumi.ui.player

import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.player.PlayerController
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.ui.playerOld.AnimPlayItemController
import com.heyanle.easybangumi.utils.stringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/4 23:28.
 * https://github.com/heyanLE
 */
class AnimPlayItemController(
    val bangumiSummary: BangumiSummary,
) {

    private var lastScope: CoroutineScope? = null

    val infoController = BangumiInfoController(bangumiSummary)

    private val _playerStatus = MutableStateFlow<AnimPlayState>(
        AnimPlayState.None
    )
    val playerStatus: StateFlow<AnimPlayState> = _playerStatus


    fun loadInfo() {
        infoController.load()
    }

    fun loadPlay(
        lineIndex: Int,
        episodeIndex: Int,
    ) {
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
        lastScope?.cancel()
        val scope = MainScope()
        lastScope = scope
        scope.launch {
            _playerStatus.emit(
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
                    _playerStatus.emit(
                        AnimPlayState.Play(
                            lineIndex,
                            episodeIndex,
                            it.data.uri,
                            it.data.type,
                        )
                    )
                }.error {
                    _playerStatus.emit(
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
                it.printStackTrace()
                _playerStatus.emit(
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


    fun release() {
        infoController.release()
        lastScope?.cancel()
    }


}

sealed class AnimPlayState(
    val sourceIndex: Int,
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

