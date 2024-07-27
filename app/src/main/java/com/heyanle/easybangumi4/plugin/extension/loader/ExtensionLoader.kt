package com.heyanle.easybangumi4.plugin.extension.loader

import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo


/**
import com.heyanle.easy_extension.Extension
 * Created by heyanlin on 2023/10/24.
 */
interface ExtensionLoader {

    /**
     * 拓展的唯一标识
     */
    val key: String

    /**
     * 加载拓展
     */
    fun load(): ExtensionInfo?

    fun canLoad(): Boolean



}