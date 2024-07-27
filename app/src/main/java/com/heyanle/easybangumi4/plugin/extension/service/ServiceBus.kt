package com.heyanle.easybangumi4.plugin.extension.service

import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo

/**
 * Created by heyanle on 2024/6/2.
 * https://github.com/heyanLE
 */
class ServiceBus(
    private val extension: ExtensionInfo.Installed
) {

    val nativeLoadService: NativeLoadService by lazy {
        NativeLoadService(extension.clazzLoader)
    }
}