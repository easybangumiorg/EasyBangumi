package com.heyanle.app_download

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by HeYanLe on 2023/8/27 22:12.
 * https://github.com/heyanLE
 */
object DownloadBus {

    class DownloadingInfo(
        val status: MutableState<Int> = mutableIntStateOf(0),
        // -1 不支持进度
        val process: MutableState<String> = mutableStateOf(""),
        val speed: MutableState<String> = mutableStateOf(""),
    )

    private val map = ConcurrentHashMap<String, DownloadingInfo>()

    fun getInfo(key: String): DownloadingInfo {
        return map.getOrPut(key) {
            DownloadingInfo()
        }
    }

}