package com.heyanle.easy_bangumi_cm.plugin.core.extension.provider

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.DataState
import com.heyanle.easy_bangumi_cm.base.utils.file_helper.JsonlFileHelper
import com.heyanle.easy_bangumi_cm.plugin.core.utils.FolderIndex
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import com.heyanle.lib.unifile.UniFileFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.File

/**
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
 * Created by heyanlin on 2024/12/17.
 */
class PkgExtensionProvider(
    workFolder: String,
    cacheFolder: String,
    // 用于调度的单线程 Scope
    val singleScope : CoroutineScope,
) : AbsExtensionProvider() {

    companion object {
        const val INDEX_FILE_NAME = "index.jsonl"
        const val FILE_SUFFIX = "eb.pkg"

        const val BASE_FILE = "base.${FILE_SUFFIX}"
        const val MANIFEST_FILE = "manifest.yaml"

        const val UNZIP_FOLDER = "unzip"
        const val ASSETS_FOLDER = "assets"

        const val CACHE_INSTALL = "install"
    }

    data class PkgExtensionIndexItem(
        val key: String,
        val lastModified: Long,
    )

    data class PkgExtensionManifest(
        // 基础信息
        val key: String,
        val label: String,
        val versionCode: Long,
        val libVersion: Int, // 纯纯看番拓展引擎版本

        // 元数据
        val readme: String? = null,
        val author: String? = null,
        val icon: String? = null,
        val map: Map<String, String> = emptyMap(),
    ) {
        companion object {
            fun fromMap(map: Map<String, String>): PkgExtensionManifest? {
                return try {
                    PkgExtensionManifest(
                        key = map["key"] ?: return null,
                        label = map["label"] ?: return null,
                        versionCode = (map["version_code"] ?: return null).toLong(),
                        libVersion = (map["lib_version"] ?: return null).toInt(),
                        readme = map["readme"],
                        author = map["author"],
                        icon = map["icon"],
                        map = map,
                    )
                } catch (e: Throwable) {
                    null
                }
            }
        }
    }

    override val type: Int
        get() = ExtensionManifest.PROVIDER_TYPE_PKG


    // 用于异步加载
    private val ioDispatcher = CoroutineProvider.io
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val workFolderFile: File by lazy { File(workFolder).apply { mkdirs() } }
    private val workIndexFile: File by lazy { File(workFolder, INDEX_FILE_NAME) }
    private val cacheFolderFile by lazy { File(cacheFolder).apply { mkdirs() } }

    private val workIndexHelper: JsonlFileHelper<PkgExtensionIndexItem> by lazy {
        JsonlFileHelper.from<PkgExtensionIndexItem>(
            folder = UniFileFactory.fromFile(workIndexFile),
            name = INDEX_FILE_NAME,
        )
    }

    // 这两个变量必须在 singleScope 中使用

    // 0->refreshJob 1->installJob 2->uninstallJob
    private var lastJobType = 0
    private var lastJob: Job? = null

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


    private suspend fun innerLoad(index: List<PkgExtensionIndexItem>): List<ExtensionManifest> {
        val result = mutableListOf<ExtensionManifest>()
        val indexUpdate = arrayListOf<PkgExtensionIndexItem>()
        index.map {
            scope.async(CoroutineProvider.io) { it to innerLoadItem(it) }
        }.map { it.await() }.forEach {  (item, manifest) ->
            if (manifest != null) {
                result.add(manifest)
                indexUpdate.add(item)
            }
        }
        workIndexHelper.set(indexUpdate)
        return result
    }

    private suspend fun innerLoadItem(indexItem: PkgExtensionIndexItem): ExtensionManifest? {
        val indexFolderFile: File = File(workFolderFile, indexItem.key)
        val pkgFile = File(indexFolderFile, BASE_FILE)
        val unzipFolder = File(indexFolderFile, UNZIP_FOLDER)

        // 1. 源文件不存在直接删除
        if(!pkgFile.exists()){
            unzipFolder.deleteRecursively()
            return null
        }

        fun getErrorManifest(
            canReinstall: Boolean = true,
            errorMsg: String
        ): ExtensionManifest {
            return ExtensionManifest(
                key = indexItem.key,
                status = if (canReinstall) ExtensionManifest.STATUS_NEED_REINSTALL else ExtensionManifest.STATUS_LOAD_ERROR,
                errorMsg = errorMsg,
                providerType = ExtensionManifest.PROVIDER_TYPE_PKG,
                loadType = ExtensionManifest.LOAD_TYPE_JS_PKG,
                sourcePath = pkgFile.absolutePath,
                assetsPath = File(unzipFolder, ASSETS_FOLDER).absolutePath,
                workPath = unzipFolder.absolutePath,
                lastModified = pkgFile.lastModified(),
            )
        }

        // 2. 检查源文件是否有变化，如果有则需要重新安装
        val isChange = pkgFile.lastModified() != indexItem.lastModified || !unzipFolder.exists() || FolderIndex.check(unzipFolder.absolutePath, indexItem.lastModified.toString())
        if (isChange) {
            unzipFolder.deleteRecursively()
            return getErrorManifest(true, "源文件已变化，请重新安装")
        }

        // 3. 读取 manifest
        val manifestFile = File(unzipFolder, MANIFEST_FILE)
        if(!manifestFile.exists()){
            unzipFolder.deleteRecursively()
            return getErrorManifest(false, "Manifest 文件不存在")
        }
        val manifestMap = getManifest(manifestFile)
        val manifest = PkgExtensionManifest.fromMap(manifestMap)
        if (manifest == null) {
            unzipFolder.deleteRecursively()
            return getErrorManifest(false, "Manifest 文件解析失败")
        }

        if (manifest.key != indexItem.key) {
            unzipFolder.deleteRecursively()
            return getErrorManifest(false, "Manifest key 不匹配")
        }



        // 4. 生成 ExtensionManifest
        return ExtensionManifest(
            key = manifest.key,
            label = manifest.label,
            versionCode = manifest.versionCode,
            libVersion = manifest.libVersion,
            readme = manifest.readme,
            author = manifest.author,
            icon = manifest.icon,
            map = manifest.map,
            providerType = ExtensionManifest.PROVIDER_TYPE_PKG,
            loadType = ExtensionManifest.LOAD_TYPE_JS_PKG,
            sourcePath = pkgFile.absolutePath,
            assetsPath = File(unzipFolder, ASSETS_FOLDER).absolutePath,
            workPath = unzipFolder.absolutePath,
            lastModified = pkgFile.lastModified(),
        )
    }

    override fun uninstall(extensionManifest: ExtensionManifest) {
        // 类型不匹配
        if (extensionManifest.providerType != ExtensionManifest.PROVIDER_TYPE_PKG) {
            return
        }
        singleScope.launch {
            lastJob?.join()
            lastJobType = 2
            lastJob = scope.launch {
                val index = workIndexHelper.get().filter { it.key != extensionManifest.key }
                workIndexHelper.set(index)
                innerFlow.update {
                    it.copy(
                        extensionManifestList = it.extensionManifestList.filter { it.key != extensionManifest.key }
                    )
                }
                val indexFolderFile = File(workFolderFile, extensionManifest.key)
                indexFolderFile.deleteRecursively()
            }
        }
    }

    override fun install(file: File, override: Boolean, callback: ((DataState<ExtensionManifest>) -> Unit)?) {
        singleScope.launch {
            lastJob?.join()
            lastJobType = 1
            lastJob = scope.launch {
                val installCache = File(cacheFolderFile, CACHE_INSTALL)
                installCache.deleteRecursively()
                installCache.mkdirs()
                try {
                    ZipFile(file).extractAll(installCache.absolutePath)
                } catch (e: ZipException) {
                    // 解压失败
                    callback?.invoke(DataState.error(e))
                    return@launch
                }

                val manifestFile = File(installCache, MANIFEST_FILE)
                if(!manifestFile.exists()){
                    // manifest 文件不存在
                    callback?.invoke(DataState.error(Throwable("Manifest 文件不存在")))
                    return@launch
                }

                val manifestMap = getManifest(manifestFile)
                val manifest = PkgExtensionManifest.fromMap(manifestMap)
                if(manifest == null){
                    // manifest 文件解析失败
                    callback?.invoke(DataState.error(Throwable("Manifest 文件解析失败")))
                    return@launch
                }

                val key = manifest.key

                // 检查是否已经安装
                if(!override && workIndexHelper.get().any { it.key == key }){
                    callback?.invoke(DataState.error(Throwable("已经安装")))
                    return@launch
                }

                val indexFolderFile = File(workFolderFile, key)

                val targetFile = File(indexFolderFile, BASE_FILE)
                val targetTemp = File(indexFolderFile, "$BASE_FILE.temp")
                val unzipFolder = File(indexFolderFile, UNZIP_FOLDER)

                targetFile.deleteRecursively()
                targetTemp.deleteRecursively()
                unzipFolder.deleteRecursively()
                unzipFolder.mkdirs()
                indexFolderFile.mkdirs()

                installCache.copyRecursively(unzipFolder, true)
                file.copyTo(targetTemp, true)
                targetTemp.renameTo(targetFile)
                FolderIndex.make(unzipFolder.absolutePath, targetFile.lastModified().toString())

                val indexItem = PkgExtensionIndexItem(key, targetFile.lastModified())
                val finManifest = innerLoadItem(indexItem)
                if(finManifest == null){
                    callback?.invoke(DataState.error(Throwable("安装失败")))
                    return@launch
                }

                // 更新 index
                workIndexHelper.set(workIndexHelper.get().filter { it.key != key } + indexItem)
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
        lastJob?.cancel()
        try {
            scope.cancel()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        innerFlow.update {
            it.copy(
                loading = true,
                extensionManifestList = emptyList()
            )
        }
    }

    private fun getManifest(file: File): Map<String, String> {
        return file.bufferedReader().use {
            it.lineSequence().map {
                val kv = it.split(":", limit = 2)
                if (kv.size == 2) {
                    kv[0].trim() to kv[1].trim()
                } else {
                    null
                }
            }.filterNotNull().toMap()
        }
    }





}