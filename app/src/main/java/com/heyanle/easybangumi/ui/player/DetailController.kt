package com.heyanle.easybangumi.ui.player

import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.ui.home.home.AnimHomeViewModel
import com.heyanle.easybangumi.utils.stringRes
import com.heyanle.lib_anim.entity.BangumiDetail
import com.heyanle.lib_anim.entity.BangumiSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow

/**
 * Created by HeYanLe on 2023/1/11 22:02.
 * https://github.com/heyanLE
 */
class DetailController(val bangumiSummary: BangumiSummary) {

    // 番剧详情状态
    sealed class DetailStatus {
        object None: DetailStatus()
        class Loading(val event: DetailEvent): DetailStatus()
        class Error(val event: DetailEvent, val errorMsg: String, val error: Throwable?): DetailStatus()
        class Completely(val event: DetailEvent, val bangumiDetail: BangumiDetail): DetailStatus()
    }

    // 刷新事件
    sealed class DetailEvent {
        object Init: DetailEvent()
        class LoadDetail(val bangumiSummary: BangumiSummary): DetailEvent()
    }

    private val eventFlow = MutableStateFlow<DetailEvent>(
        DetailEvent.Init
    )

    private val _detailStatus = channelFlow<DetailStatus> {
        eventFlow.collectLatest {detail ->
            when(detail){
                DetailEvent.Init -> {
                    send(DetailStatus.None)
                }
                is DetailEvent.LoadDetail -> {
                    kotlin.runCatching {
                        send(DetailStatus.Loading(detail))
                        AnimSourceFactory.requireDetail(detail.bangumiSummary.source).detail(detail.bangumiSummary)
                            .complete {
                                send(DetailStatus.Completely(detail, it.data))
                            }.error {
                                it.throwable.printStackTrace()
                                send(DetailStatus.Error(detail, if(it.isParserError) stringRes(R.string.source_error) else stringRes(R.string.loading_error), it.throwable))
                            }
                    }.onFailure {
                        it.printStackTrace()
                        send(DetailStatus.Error(detail, stringRes(R.string.loading_error), it))
                    }
                }
            }
        }
    }
    val detailFlow: Flow<DetailStatus> = _detailStatus

    suspend fun load(){
        eventFlow.emit(DetailEvent.LoadDetail(bangumiSummary))
    }

}