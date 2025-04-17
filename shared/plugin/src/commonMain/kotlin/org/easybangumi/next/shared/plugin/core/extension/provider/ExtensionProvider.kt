package org.easybangumi.next.shared.plugin.core.extension.provider

import kotlinx.coroutines.flow.StateFlow
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.plugin.api.extension.ExtensionManifest

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

interface ExtensionProvider {

    val type: Int

    val flow: StateFlow<DataState<List<ExtensionManifest>>>

    fun refresh()

    fun uninstall(extensionManifestList: List<ExtensionManifest>)

    // 必须运行在 io 线程
    fun install(
        file: List<UFD>,
        override: Boolean,
        callback: (List<DataState<ExtensionManifest>>) -> Unit
    )

    fun release()

}