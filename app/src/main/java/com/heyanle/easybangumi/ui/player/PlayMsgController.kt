package com.heyanle.easybangumi.ui.player

import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.utils.stringRes
import com.heyanle.lib_anim.entity.BangumiSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow

/**
 * Created by HeYanLe on 2023/1/11 22:21.
 * https://github.com/heyanLE
 */
class PlayMsgController(val bangumiSummary: BangumiSummary) {

    sealed class PlayMsgStatus {
        object None: PlayMsgStatus()
        class Loading(val event: PlayMsgEvent):PlayMsgStatus()
        class Error(val event: PlayMsgEvent, val errorMsg: String, val error: Throwable?):PlayMsgStatus()
        class Completely(val event: PlayMsgEvent, val playMsg: LinkedHashMap<String, List<String>>):PlayMsgStatus()
    }

    // 刷新事件
    sealed class PlayMsgEvent {
        object Init: PlayMsgEvent()
        class LoadDetail(val bangumiSummary: BangumiSummary): PlayMsgEvent()
    }

    private val eventFlow = MutableStateFlow<PlayMsgEvent>(
        PlayMsgController.PlayMsgEvent.Init
    )

    private val _detailStatus = channelFlow<PlayMsgStatus> {
        eventFlow.collectLatest {event ->
            when(event){
                PlayMsgEvent.Init -> {
                    send(PlayMsgStatus.None)
                }
                is PlayMsgEvent.LoadDetail -> {
                    kotlin.runCatching {
                        send(PlayMsgStatus.Loading(event))
                        AnimSourceFactory.requirePlay(event.bangumiSummary.source).getPlayMsg(event.bangumiSummary)
                            .complete {
                                send(PlayMsgStatus.Completely(event, it.data))
                            }.error {
                                send(PlayMsgStatus.Error(event, if(it.isParserError) stringRes(
                                    R.string.source_error) else stringRes(R.string.loading_error), it.throwable))
                            }
                    }.onFailure {
                        send(PlayMsgStatus.Error(event, stringRes(R.string.loading_error), it))
                    }
                }
            }
        }
    }
    val flow: Flow<PlayMsgStatus> = _detailStatus

    suspend fun load(){
        eventFlow.emit(PlayMsgEvent.LoadDetail(bangumiSummary))
    }

}