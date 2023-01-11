package com.heyanle.easybangumi.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.utils.stringRes
import com.heyanle.lib_anim.entity.BangumiDetail
import com.heyanle.lib_anim.entity.BangumiSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/11 16:36.
 * https://github.com/heyanLE
 */
class AnimPlayViewModel(
    val bangumiSummary: BangumiSummary,
): ViewModel() {

    val curEpisode: HashMap<Int, Int> = hashMapOf()


    val detailController = DetailController(bangumiSummary)
    val playMsgController = PlayMsgController(bangumiSummary)


    // 播放 Url 真正播放器的状态由 EasyPlayer 自己维护
    sealed class PlayerStatus (
        val sourceIndex: Int, val episode: Int
    ){
        class None(sourceIndex: Int, episode: Int): PlayerStatus(sourceIndex, episode)

        class Loading(sourceIndex: Int, episode: Int): PlayerStatus(sourceIndex, episode)
        class Play(sourceIndex: Int, episode: Int, val url: String): PlayerStatus(sourceIndex, episode)

        class Error(sourceIndex: Int, episode: Int, val errorMsg: String, val throwable: Throwable?):PlayerStatus(sourceIndex, episode)
    }



    // 事件
    sealed class PlayerEvent {
        class None(val lineIndex: Int, val episode: Int): PlayerEvent()
        class ChangePlay(val lineIndex: Int, val episode: Int): PlayerEvent()

        class ChangeLine(val lineIndex: Int): PlayerEvent()
    }


    private val eventFlow = MutableStateFlow<PlayerEvent>(
        PlayerEvent.None(0, 0)
    )

    private val _playerStatus = channelFlow <PlayerStatus> {
        eventFlow.collectLatest { event ->
            when(event){
                is PlayerEvent.None -> {
                    send(PlayerStatus.None(event.lineIndex, event.episode))
                }
                is PlayerEvent.ChangePlay -> {
                    curEpisode[event.lineIndex] = event.episode
                    send(PlayerStatus.Loading(event.lineIndex, event.episode))
                    kotlin.runCatching {
                        AnimSourceFactory.requirePlay(bangumiSummary.source).getPlayUrl(bangumiSummary, event.lineIndex, event.episode)
                            .complete {
                                send(PlayerStatus.Play(event.lineIndex, event.episode, it.data))
                            }.error {
                                send(PlayerStatus.Error(event.lineIndex, event.episode, if(it.isParserError) stringRes(
                                    R.string.source_error) else stringRes(R.string.loading_error), it.throwable))
                            }
                    }.onFailure {
                        send(PlayerStatus.Error(event.lineIndex, event.episode, stringRes(R.string.loading_error), it))
                    }

                }
                is PlayerEvent.ChangeLine -> {
                    eventFlow.emit(PlayerEvent.ChangePlay(event.lineIndex, curEpisode.get(event.lineIndex)?:0))
                }
            }
        }
    }
    val playerStatus: Flow<PlayerStatus> = _playerStatus

    fun load(){
        viewModelScope.launch {
            detailController.load()
            playMsgController.load()
        }
    }

    fun changeLines(lineIndex: Int){
        viewModelScope.launch {
            eventFlow.emit(PlayerEvent.ChangeLine(lineIndex))
        }
    }

    fun changePlayer(lineIndex: Int, episode: Int){
        viewModelScope.launch {
            eventFlow.emit(PlayerEvent.ChangePlay(lineIndex, episode))
        }
    }
}

class AnimPlayerViewModelFactory(
    val source: String,
    val detailUrl: String,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnimPlayViewModel::class.java))
            return AnimPlayViewModel(BangumiSummary(source, detailUrl)) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}