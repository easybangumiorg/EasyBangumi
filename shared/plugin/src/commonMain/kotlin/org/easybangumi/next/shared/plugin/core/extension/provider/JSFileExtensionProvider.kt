package org.easybangumi.next.shared.plugin.core.extension.provider

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.io.files.FileNotFoundException
import okio.IOException
import okio.buffer
import okio.use
import org.easybangumi.next.lib.store.file_helper.json.JsonlFileHelper
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.copyTo
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.plugin.extension.ExtensionManifest
import org.easybangumi.next.shared.plugin.javascript.JsHelper
import kotlin.invoke
import kotlin.text.get

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
 *  不支持 assets，直接单文件存储即可
 *  直接使用 lastModified 判断是否需要重新加载
 *  workFolder 工作路径
 *   |- index.jsonl              -> 记录所有的 key 和 lastModified
 *   |- key1.js                  -> 拓展文件
 *   |- key2.jsc                 -> 加密的拓展文件
 *   |- ...
 */

class JSFileExtensionProvider(
    private val workerFolder: UniFile,
    val scope: CoroutineScope,
    // 必须为单线程调度的协程上下文
    val singleDispatcher: CoroutineDispatcher,
): AbsExtensionProvider() {

    companion object {
        const val INDEX_FILE_NAME = "index.jsonl"
        const val FILE_SUFFIX = "eb.js"
        const val FILE_CRY_SUFFIX = "eb.jsc"
    }


    data class JsExtensionIndexItem(
        val key: String,
        val type: Int,
        val lastModified: Long,
    ) {
        companion object {
            const val TYPE_JS = 1
            const val TYPE_JSC = 2
        }

        fun getFileName(): String {
            return "${key}.${if (type == TYPE_JS) FILE_SUFFIX else if (type == TYPE_JSC) FILE_CRY_SUFFIX else ""}"
        }
    }

    override val type: Int = ExtensionManifest.PROVIDER_TYPE_JS_FILE


    private val indexHelper = JsonlFileHelper<JsExtensionIndexItem> (
        workerFolder.getUFD(),
        INDEX_FILE_NAME,
        JsExtensionIndexItem::class
    )

    private var refreshJob: Job? = null
    private var installJob: Job? = null



    override fun refresh() {
        scope.launch(singleDispatcher) {
            installJob?.join()
            refreshJob?.cancelAndJoin()
            refreshJob = scope.launch {
                innerRefresh()
            }
        }
    }


    override fun uninstall(extensionManifestList: List<ExtensionManifest>) {
        scope.launch(singleDispatcher) {
            installJob?.join()
            refreshJob?.cancelAndJoin()
            installJob = scope.launch {
                indexHelper.update {
                    it.filter { item -> !extensionManifestList.any { it.key == item.key } }
                }
                // 删文件异步
                scope.launch {
                    extensionManifestList.forEach {
                        val fileName = it.key
                        val targetFile = workerFolder.child(fileName)
                        if (targetFile != null && targetFile.exists()) {
                            targetFile.delete()
                        }
                    }
                }

            }
        }
    }

    override fun install(file: List<UFD>, override: Boolean, callback: (List<DataState<ExtensionManifest>>) -> Unit) {
        scope.launch(singleDispatcher) {
            installJob?.join()
            refreshJob?.cancelAndJoin()
            installJob = scope.launch {
                val res = file.map {
                    innerInstall(it, override)
                }

                val callbackRes = res.map {
                    it.map {
                        it.first
                    }
                }

                val itemList = res.map {
                    it.map {
                        it.second
                    }
                }.filterIsInstance<DataState.Ok<JsExtensionIndexItem>>().map {
                    it.data
                }

                indexHelper.update {
                    it.filter { item -> !itemList.any { it.key == item.key } } + itemList
                }

                refresh()

                callback(callbackRes)
            }
        }
    }


    private suspend fun innerInstall(file: UFD, override: Boolean): DataState<Pair<ExtensionManifest, JsExtensionIndexItem>>{
        val uniFile = UniFileFactory.fromUFD(file)
        if (uniFile == null) {
            return DataState.error("UFD error", IllegalArgumentException("ufd error ${file.toString()}"))
        }
        if (!uniFile.exists()) {
            return DataState.error("file not found", FileNotFoundException(uniFile.getFilePath()))
        }
        val bufferedSource = uniFile.openSource().buffer()
        var isCry = false
        val manifestMap = bufferedSource.peek().use {
            isCry = JsHelper.isSourceCry(it)
            if (isCry) JsHelper.getManifestFromCry(it) else JsHelper.getManifestFromNormal(it)

        }

        val key = manifestMap["key"]
        if (key.isNullOrEmpty()) {
            return DataState.error("key is null")
        }

        // 检查是否已经安装
        if(!override && indexHelper.get().find { it.key == key } != null){
            return DataState.error("has install without override", Throwable("${key} has install without override"))
        }

        val targetCryFile = workerFolder.child("${key}.${FILE_CRY_SUFFIX}")
        val targetJsFile = workerFolder.child("${key}.${FILE_SUFFIX}")

        targetJsFile?.delete()
        targetCryFile?.delete()

        val targetFile = if (isCry) {
            targetCryFile
        } else {
            targetJsFile
        }

        if (targetFile == null) {
            return DataState.error("target file error", IllegalStateException("target file error"))
        }

        val sink = targetFile.openSink(false).buffer()
        sink.use {  sink ->
            bufferedSource.use {
                it.copyTo(sink)
            }
        }

        if (targetFile.length() == 0L) {
            return DataState.error("copy error", IOException("copy error"))
        }

        val item = JsExtensionIndexItem(
            key = key,
            type = if (isCry) JsExtensionIndexItem.TYPE_JSC else JsExtensionIndexItem.TYPE_JS,
            lastModified = targetFile.lastModified()
        )

        // 直接解析复制后的文件，顺带做一个黑盒测试吧
        val manifest = innerLoadItem(item)

        if (manifest == null) {
            return DataState.error("manifest parse error", IOException("install error"))
        }


        return DataState.ok(manifest to JsExtensionIndexItem(
            key = key,
            type = if (isCry) JsExtensionIndexItem.TYPE_JSC else JsExtensionIndexItem.TYPE_JS,
            lastModified = targetFile.lastModified()
        ))


//        // 需不需要预加载？
////        val finManifest = innerLoadItem(item)
////        if(finManifest == null){
////            callback.invoke(DataState.error("install error reboot can retry"))
////            return
////        }
//
//        indexHelper.update {
//            it.filter { it.key != key } + item
//        }
//
//        refresh()

    }

    override fun release() {

    }

    private suspend fun innerRefresh(){
        val list = indexHelper.get()
        fireLoading()
        if (list.isEmpty()) {
            fireData(emptyList())
            return
        }
        val res = innerLoad(list)
        yield()
        fireData(res)
    }


    private suspend fun innerLoad(index: List<JsExtensionIndexItem>): List<ExtensionManifest> {
        val result = mutableListOf<ExtensionManifest>()
        val indexUpdate = arrayListOf<JsExtensionIndexItem>()
        index.map {
            scope.async() { it to innerLoadItem(it) }
        }.map {
            yield()
            it.await()
        }.forEach { (item, manifest) ->
            if (manifest != null) {
                result.add(manifest)
                indexUpdate.add(item)
            }
        }
        indexHelper.set(indexUpdate)
        return result
    }

    private suspend fun innerLoadItem(item: JsExtensionIndexItem): ExtensionManifest? {
        val fileName = item.getFileName()
        if (fileName.isEmpty()) {
            return null
        }



        val file = workerFolder.child(fileName)
        if (file == null || !file.exists() || !file.isFile()) {
            return null
        }

        fun getErrorManifest(
            canReinstall: Boolean = true,
            errorMsg: String
        ): ExtensionManifest {
            return ExtensionManifest(
                key = item.key,
                status = if (canReinstall) ExtensionManifest.STATUS_NEED_REINSTALL else ExtensionManifest.STATUS_LOAD_ERROR,
                errorMsg = errorMsg,
                providerType = ExtensionManifest.PROVIDER_TYPE_JS_FILE,
                loadType = ExtensionManifest.LOAD_TYPE_JS_FILE,
                sourcePath = file.getUFD(),
                assetsPath = null,
                workPath = file.getUFD(),
                lastModified = file.lastModified(),
            )
        }

        if (file.lastModified() != item.lastModified) {
            return getErrorManifest(
                canReinstall = true,
                errorMsg = "文件已被修改，需要重新安装"
            )
        }

        val manifest = file.openSource().buffer().use {
            if (item.type == JsExtensionIndexItem.TYPE_JSC) {
                JsHelper.getManifestFromCry(it)
            } else if (item.type == JsExtensionIndexItem.TYPE_JS) {
                JsHelper.getManifestFromNormal(it)
            } else {
                null
            }
        } ?: return getErrorManifest(
            canReinstall = false,
            errorMsg = "元数据解析失败"
        )

        val key = manifest["key"]
        if (key != item.key) {
            return getErrorManifest(
                canReinstall = false,
                errorMsg = "元数据 key 不匹配"
            )
        }

        return ExtensionManifest(
            key = item.key,
            status = ExtensionManifest.STATUS_CAN_LOAD,
            errorMsg = null,
            label = manifest["label"] ?: "",
            readme = manifest["readme"],
            author = manifest["author"],
            icon = manifest["icon"],
            versionCode = manifest["version_code"]?.toLongOrNull() ?: 0,
            libVersion = manifest["lib_version"]?.toIntOrNull() ?: 0,
            map = manifest,
            providerType = ExtensionManifest.PROVIDER_TYPE_JS_FILE,
            loadType = ExtensionManifest.LOAD_TYPE_JS_FILE,
            sourcePath = file.getUFD(),
            assetsPath = null,
            workPath = file.getUFD(),
            lastModified = item.lastModified,
            ext = null
        )
    }



}