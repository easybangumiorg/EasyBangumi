package com.heyanle.easybangumi4.download

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by HeYanLe on 2023/8/27 22:12.
 * https://github.com/heyanLE
 */
class DownloadBus() {

    class DownloadingInfo(
        val status: MutableState<Int> = mutableIntStateOf(0),
        // -1 不支持进度
        val process: MutableState<Float> = mutableFloatStateOf(0f),
        val speed: MutableState<Float> = mutableFloatStateOf(0f),
    )

    private val map = ConcurrentHashMap<String, DownloadingInfo>()

    fun getInfo(key: String): DownloadingInfo {
        return map.getOrPut(key) {
            DownloadingInfo()
        }
    }

}