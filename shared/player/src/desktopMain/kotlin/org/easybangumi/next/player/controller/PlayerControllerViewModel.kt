package org.easybangumi.next.player.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.libplayer.api.PlayerBridge
import org.easybangumi.next.libplayer.api.VideoSize
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class PlayerControllerViewModel(
    private val bridge: PlayerBridge,
) : BaseViewModel() {

    companion object {
        const val CONTROL_HIDE_DELAY = 4000L
        const val POSITION_LOOP_DELAY = 1000L

    }

    // 普通状态  长按加速中 锁定中 左右滑动中 上下滑动中 结束
    enum class ControlState {
        Normal, Locked, HorizontalScroll, Ended
    }


}