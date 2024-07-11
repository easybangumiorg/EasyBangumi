package com.heyanle.easybangumi4.cartoon.download

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import com.heyanle.easybangumi4.bus.DownloadingBus
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by HeYanLe on 2023/9/17 15:39.
 * https://github.com/heyanLE
 */
class CartoonDownloadBus (
    private val downloadBus: DownloadingBus,
){


    fun getInfo(key: String): DownloadingBus.DownloadingInfo {
        return downloadBus.getInfo(DownloadingBus.DownloadScene.CARTOON, key)
    }

    fun remove(key: String) {
        downloadBus.remove(DownloadingBus.DownloadScene.CARTOON, key = key)
    }
}