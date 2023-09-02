package com.heyanle.easybangumi4.download

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by HeYanLe on 2023/8/27 22:12.
 * https://github.com/heyanLE
 */
class DownloadBus() {

    class DownloadingInfo(
        val status: MutableState<String> = mutableStateOf(""),
        // -1 不支持进度
        val process: MutableState<Float> = mutableFloatStateOf(0f),
        val subStatus: MutableState<String> = mutableStateOf(""),
    )

    private val map = ConcurrentHashMap<String, DownloadingInfo>()

    fun getInfo(key: String): DownloadingInfo {
        return map.getOrPut(key) {
            DownloadingInfo()
        }
    }

    fun remove(key: String) {
        map.remove(key = key)
    }

}