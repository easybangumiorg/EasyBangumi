package com.heyanle.easybangumi.ui.player

import androidx.lifecycle.ViewModel
import com.heyanle.lib_anim.entity.BangumiDetail
import com.heyanle.lib_anim.entity.BangumiSummary

/**
 * Created by HeYanLe on 2023/1/11 16:36.
 * https://github.com/heyanLE
 */
class AnimPlayViewModel: ViewModel() {

    sealed class DetailStatus {
        object None: DetailStatus()
        class Loading(val loadingDetail: PageEvent.LoadDetail): DetailStatus()
        class Error(val loadingDetail: PageEvent.LoadDetail, val errorMsg: String, val error: Throwable?): DetailStatus()
        class Completely(val loadingDetail: PageEvent.LoadDetail, val bangumiDetail: BangumiDetail): DetailStatus()
    }

    sealed class PlayMsgStatus {
        object None: PlayMsgStatus()
        class Loading(val loadPlayMsg: PageEvent.LoadPlayMsg)
        class Error(val loadPlayMsg: PageEvent.LoadPlayMsg, val errorMsg: String, val error: Throwable?)
        class Completely(val loadPlayMsg: PageEvent.LoadPlayMsg, val playMsg: LinkedHashMap<String, List<String>>)
    }

    sealed class PlayerStatus {
        object None: PlayMsgStatus()
        class Play(val url: String): PlayMsgStatus()
    }



    sealed class PageEvent {
        class LoadDetail(val bangumiSummary: BangumiSummary)
        class LoadPlayMsg(val bangumiSummary: BangumiSummary)
        class ChangePlaySource(val sourceIndex: Int, val episode: Int)
    }
}