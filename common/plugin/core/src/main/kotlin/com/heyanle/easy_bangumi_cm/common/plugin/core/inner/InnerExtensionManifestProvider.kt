package com.heyanle.easy_bangumi_cm.common.plugin.core.inner

import com.heyanle.easy_bangumi_cm.base.model.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest


/**
 * Created by HeYanLe on 2025/2/5 22:29.
 * https://github.com/heyanLE
 */

class InnerExtensionManifestProvider(
    val pathProvider: IPathProvider,
){

    val extensionManifest = ExtensionManifest(
        key = "inner",
        status = ExtensionManifest.STATUS_CAN_LOAD,
        errorMsg = null,

        label = "内置拓展",
        readme = null,
        author = "HeYanLe",
        icon = null,
        versionCode = 1,
        libVersion = 1,
        map = emptyMap(),

        providerType = ExtensionManifest.PROVIDER_TYPE_INNER,
        loadType = ExtensionManifest.LOAD_TYPE_INNER,

        sourcePath = null,
        assetsPath = null,

        workPath = pathProvider.getFilePath("inner_ext"),

        lastModified = System.currentTimeMillis(),
    )

}