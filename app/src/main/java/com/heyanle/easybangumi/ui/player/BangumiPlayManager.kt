package com.heyanle.easybangumi.ui.player

import androidx.lifecycle.MutableLiveData
import com.heyanle.easybangumi.player.PlayerController
import com.heyanle.easybangumi.player.PlayerTinyController
import com.heyanle.easybangumi.ui.common.easy_player.BaseEasyPlayerView
import com.heyanle.easybangumi.ui.common.easy_player.EasyPlayerView
import com.heyanle.easybangumi.ui.playerOld.AnimPlayItemController
import java.lang.ref.WeakReference

/**
 * Created by HeYanLe on 2023/2/5 0:13.
 * https://github.com/heyanLE
 */
object BangumiPlayManager {

    data class EnterData(
        val sourceIndex: Int = -1,
        val episode: Int = -1,
        val startProcess: Long = -1L,
    ) {}

    private var composeViewRes: WeakReference<EasyPlayerView>? = null

    private var tinyViewRes: WeakReference<BaseEasyPlayerView>? = null

    val curAnimPlayItemController = MutableLiveData<AnimPlayItemController>()

    init {
        PlayerController.playerControllerStatus.observeForever { state ->
            if (PlayerTinyController.isTinyMode) {
                this.tinyViewRes?.get()?.dispatchPlayStateChange(state)
            } else {
                this.composeViewRes?.get()?.basePlayerView?.dispatchPlayStateChange(state)
            }

        }
    }

}