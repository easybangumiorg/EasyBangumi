package com.heyanle.easy_bangumi_cm.plugin.core.extension.loader

import com.heyanle.easy_bangumi_cm.plugin.core.entity.ExtensionInfo
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest

/**
 * Created by heyanlin on 2024/12/13.
 */
interface ExtensionLoader {

    fun canLoad(extensionManifest: ExtensionManifest): Boolean

    suspend fun load(extensionManifest: ExtensionManifest): ExtensionInfo

}