package com.heyanle.easybangumi4.extension.loader

import android.content.pm.PackageManager
import com.heyanle.easybangumi4.extension.Extension

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
    fun load(): Extension?

    fun canLoad(): Boolean



}