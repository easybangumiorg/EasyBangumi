package org.easybangumi.next.shared.plugin.core.extension.provider

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
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
 *
 * pkg 类型插件提供者，为一个 zip 包，内部包含 manifest.json 和 assets 文件夹
 * cacheFolder 缓存路径 用于暂存文件读元数据
 * workFolder 工作路径
 *  |- index.jsonl              -> 记录所有的 key 和 lastModified
 *  |- 'key1'                   -> 每个 key 一个文件夹
 *    |- base.eb.pkg            -> 拓展原始文件，为 zip 包
 *    |- unzip
 *      |- .folder_index.jsonl  -> 记录文件夹的清单文件，用于判断文件是否有变化
 *      |- manifest.yaml        -> 拓展的 manifest 文件，provider 只依赖里面的 key 信息，其他信息交给 loader 处理
 *      |- assets               -> 资源文件夹
 *      |- ...                  -> 其他业务文件，具体交给 loader 处理
 *
 *
 *  |- 'key2'                   -> 每个 key 一个文件夹
 *    |- ...
 */

class JSPkgExtensionProvider(
    private val workerFolder: UniFile,
    private val cacheFolder: UniFile,
    val scope: CoroutineScope,

    // 必须为单线程调度的协程上下文
    val singleDispatcher: CoroutineDispatcher,
) : AbsExtensionProvider() {

    override val type: Int = ExtensionManifest.PROVIDER_TYPE_JS_PKG

    init {
        // TODO 后续支持
        fireData(emptyList())
    }

    override fun refresh() {
    }

    override fun uninstall(extensionManifestList: List<ExtensionManifest>) {

    }

    override fun install(
        file: List<UFD>,
        override: Boolean,
        callback: (List<DataState<ExtensionManifest>>) -> Unit
    ) {

    }

    override fun release() {

    }
}