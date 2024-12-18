package com.heyanle.easy_bangumi_cm.plugin.core.extension.provider

import com.heyanle.easy_bangumi_cm.base.data.DataState
import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.file_helper.JsonlFileHelper
import com.heyanle.easy_bangumi_cm.plugin.core.cry.JsCryHelper
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import com.heyanle.easy_bangumi_cm.unifile.UniFileFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import java.io.BufferedReader
import java.io.File
import java.io.InputStream

/**
 * 纯js 文件比较简单，不支持 assets，直接单文件存储即可
 * 并且直接使用 lastModified 判断是否需要重新加载
 * workFolder 工作路径
 *  |- index.jsonl              -> 记录所有的 key 和 lastModified
 *  |- key1.js                  -> 拓展文件
 *  |- key2.jsc                 -> 加密的拓展文件
 *  |- ...
 * Created by heyanlin on 2024/12/13.
 */
class JSFileExtensionProvider(
    workFolder: String,
    // 用于调度的单线程 Scope
    val singleScope: CoroutineScope,
) : AbsExtensionProvider() {

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

    // 用于异步加载
    private val ioDispatcher = CoroutineProvider.io
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val workFolderFile: File by lazy { File(workFolder).apply { mkdirs() } }
    private val workIndexFile: File by lazy { File(workFolder, INDEX_FILE_NAME) }

    private val workIndexHelper: JsonlFileHelper<JsExtensionIndexItem> by lazy {
        JsonlFileHelper.from<JsExtensionIndexItem>(
            folder = UniFileFactory.fromFile(workIndexFile),
            name = INDEX_FILE_NAME,
        )
    }

    // 0->refreshJob 1->installJob 2->uninstallJob
    private var lastJobType = 0
    private var lastJob: Job? = null

    override val type: Int
        get() = ExtensionManifest.PROVIDER_TYPE_JS_FILE

    override fun refresh() {
        singleScope.launch {
            if (lastJobType == 0) {
                lastJob?.cancelAndJoin()
            } else {
                lastJob?.join()
            }
            lastJobType = 0
            lastJob = scope.launch {
                val index = workIndexHelper.get()
                val res = innerLoad(index)
                innerFlow.update {
                    it.copy(
                        loading = false,
                        extensionManifestList = res
                    )
                }
            }
        }
    }

    override fun uninstall(extensionManifest: ExtensionManifest) {
        singleScope.launch {
            if (lastJobType == 2) {
                lastJob?.cancelAndJoin()
            } else {
                lastJob?.join()
            }
            lastJobType = 2
            lastJob = scope.launch {
                val key = extensionManifest.key
                val item = workIndexHelper.get().find { it.key == key }
                if (item != null) {
                    File(workFolderFile, item.getFileName()).delete()
                    File(workFolderFile, "${key}.${FILE_CRY_SUFFIX}").delete()
                    File(workFolderFile, "${key}.${FILE_SUFFIX}").delete()
                    workIndexHelper.set(workIndexHelper.get().filter { it.key != key })
                    innerFlow.update {
                        it.copy(
                            extensionManifestList = it.extensionManifestList.filter { it.key != key }
                        )
                    }
                }
            }
        }
    }

    override fun install(file: File, override: Boolean, callback: ((DataState<ExtensionManifest>) -> Unit)?) {
        singleScope.launch {
            if (lastJobType == 1) {
                lastJob?.cancelAndJoin()
            } else {
                lastJob?.join()
            }
            lastJobType = 1
            lastJob = scope.launch {
                val isCry = file.inputStream().use {
                    JsCryHelper.isCryJs(it)
                }

                val key = if (isCry) {
                    JsCryHelper.getManifest(file)?.get("key")
                } else {
                    file.inputStream().bufferedReader().use {
                        getJsManifest(it)["key"]
                    }
                }

                if (key.isNullOrEmpty()) {
                    callback?.invoke(DataState.error("key 为空"))
                    return@launch
                }

                // 检查是否已经安装
                if(!override && workIndexHelper.get().any { it.key == key }){
                    callback?.invoke(DataState.error(Throwable("已经安装")))
                    return@launch
                }

                File(workFolderFile, "${key}.${FILE_CRY_SUFFIX}").delete()
                File(workFolderFile, "${key}.${FILE_SUFFIX}").delete()

                val targetFile = File(workFolderFile, "${key}.${if (isCry) FILE_CRY_SUFFIX else FILE_SUFFIX}")
                file.copyTo(targetFile, overwrite = true)
                val item = JsExtensionIndexItem(
                    key = key,
                    type = if (isCry) JsExtensionIndexItem.TYPE_JSC else JsExtensionIndexItem.TYPE_JS,
                    lastModified = targetFile.lastModified()
                )

                val finManifest = innerLoadItem(item)
                if(finManifest == null){
                    callback?.invoke(DataState.error("安装失败"))
                    return@launch
                }


                // 更新 index
                workIndexHelper.set(workIndexHelper.get().filter { it.key != key } + item)
                innerFlow.update {
                    it.copy(
                        extensionManifestList = it.extensionManifestList.filter { it.key != key } + finManifest
                    )
                }
                callback?.invoke(DataState.Ok(finManifest))

            }
        }
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    private suspend fun innerLoad(index: List<JsExtensionIndexItem>): List<ExtensionManifest> {
        val result = mutableListOf<ExtensionManifest>()
        val indexUpdate = arrayListOf<JsExtensionIndexItem>()
        index.map {
            scope.async(CoroutineProvider.io) { it to innerLoadItem(it) }
        }.map { it.await() }.forEach { (item, manifest) ->
            if (manifest != null) {
                result.add(manifest)
                indexUpdate.add(item)
            }
        }
        workIndexHelper.set(indexUpdate)
        return result
    }

    private suspend fun innerLoadItem(item: JsExtensionIndexItem): ExtensionManifest? {
        val fileName = item.getFileName()
        if (fileName.isEmpty()) {
            return null
        }

        val file = File(workFolderFile, fileName)
        if (!file.exists()) {
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
                sourcePath = file.absolutePath,
                assetsPath = null,
                workPath = file.absolutePath,
                lastModified = file.lastModified(),
            )
        }

        if (file.lastModified() != item.lastModified) {
            return getErrorManifest(
                canReinstall = true,
                errorMsg = "文件已被修改，需要重新安装"
            )
        }
        val manifest = when (item.type) {
            JsExtensionIndexItem.TYPE_JS -> file.bufferedReader().use { getJsManifest(it) }
            JsExtensionIndexItem.TYPE_JSC -> JsCryHelper.getManifest(file)
            else -> return null
        } ?: return getErrorManifest(
            canReinstall = true,
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
            versionCode = manifest["versionCode"]?.toLongOrNull() ?: 0,
            libVersion = manifest["libVersion"]?.toIntOrNull() ?: 0,
            providerType = ExtensionManifest.PROVIDER_TYPE_JS_FILE,
            loadType = ExtensionManifest.LOAD_TYPE_JS_FILE,
            sourcePath = file.absolutePath,
            assetsPath = null,
            workPath = file.absolutePath,
            lastModified = item.lastModified,
            ext = null
        )
    }


    private fun getJsManifest(bufferedReader: BufferedReader): Map<String, String> {
        val i = bufferedReader.lineSequence()
        val map = mutableMapOf<String, String>()
        for (it in i) {
            if (it.startsWith("//")) {
                val atIndex = it.indexOf("@")
                val spaceIndex = it.indexOf(" ")
                if (atIndex != -1 && spaceIndex != -1) {
                    val key = it.substring(atIndex + 1, spaceIndex)
                    val value = it.substring(spaceIndex + 1)
                    map[key] = value
                }
            } else {
                break
            }
        }
        return map

    }

}