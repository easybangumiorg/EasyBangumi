package com.heyanle.easybangumi4.ui.common.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.ExoPlayer

/**
 * Created by HeYanLe on 2023/3/8 22:45.
 * https://github.com/heyanLE
 */
class ControlViewModel(
    private val exoPlayer: ExoPlayer
): ViewModel() {

    // 状态 长按加速中 锁定中 左右滑动中 上下滑动中 普通

    enum class ControlState {
        Normal, Locked, HorizontalScroll, VerticalDrag, LongTouched
    }

    var controlState by mutableStateOf(ControlState.Normal)

    var isNormalShow by mutableStateOf(true)

    var horizontalScrollPosition by mutableStateOf(0L)

    enum class VerticalDrag {
        None, BRIGHTNESS, VOLUME
    }

    var verticalScrollType by mutableStateOf(VerticalDrag.None)
    var verticalDragPercent by mutableStateOf(0F)

    var isLockedShow by mutableStateOf(false)


    var isFullScreen by mutableStateOf(false)

    var isLoading by mutableStateOf(false)

    var position by mutableStateOf(0L)
    var bufferPosition by mutableStateOf(0L)

    var during by mutableStateOf(0L)



    fun onLockedChange(locked: Boolean){}

    fun onFullScreen(fullScreen: Boolean){}

    fun onPlayPause(isPlay: Boolean){}

    fun onPrepare(){}

    fun onPositionChange(position: Long){}

    fun onPositionChangeFinished(){}

    fun onHideClick(){}


}