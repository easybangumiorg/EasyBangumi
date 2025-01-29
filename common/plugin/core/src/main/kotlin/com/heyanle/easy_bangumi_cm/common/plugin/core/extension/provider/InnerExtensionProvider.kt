package com.heyanle.easy_bangumi_cm.common.plugin.core.extension.provider

import com.heyanle.easy_bangumi_cm.base.utils.DataState
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import java.io.File


/**
 * Created by HeYanLe on 2024/12/8 22:57.
 * https://github.com/heyanLE
 */

class InnerExtensionProvider: AbsExtensionProvider() {

    override val type: Int
        get() = ExtensionManifest.PROVIDER_TYPE_INNER

    override fun refresh() {

    }

    override fun uninstall(extensionManifest: ExtensionManifest) {

    }

    override fun install(file: File, override: Boolean, callback: ((DataState<ExtensionManifest>) -> Unit)?) {
        callback?.invoke(DataState.error("不支持安装"))
    }

    override fun release() { }
}