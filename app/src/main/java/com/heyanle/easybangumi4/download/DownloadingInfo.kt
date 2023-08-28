package com.heyanle.easybangumi4.download

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf

/**
 * 用于通知 compose 刷新
 * Created by HeYanLe on 2023/8/27 22:12.
 * https://github.com/heyanLE
 */
class DownloadingInfo(
    val status: MutableState<Int> = mutableIntStateOf(0),
    val process: MutableState<Float> = mutableFloatStateOf(0f),
    val speed: MutableState<Float> = mutableFloatStateOf(0f),
)