package com.heyanle.easybangumi4.bus

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import java.util.concurrent.ConcurrentHashMap

/**
 * 下载进度分发
 * Created by heyanlin on 2023/11/16.
 */
class DownloadingBus {

    // 下载场景
    enum class DownloadScene {
        CARTOON, EXTENSION
    }

    class DownloadingInfo(
        val status: MutableState<String> = mutableStateOf(""),
        val subStatus: MutableState<String> = mutableStateOf(""),
        val process: MutableState<Float> = mutableFloatStateOf(-1f)
    )
    private val map = ConcurrentHashMap<String, DownloadingInfo>()

    fun getInfo(scene: DownloadScene, key: String): DownloadingInfo {
        return map.getOrPut("${scene}-${key}") {
            DownloadingInfo()
        }
    }

    fun remove(scene: DownloadScene, key: String) {
        map.remove("${scene}-${key}")
    }
}