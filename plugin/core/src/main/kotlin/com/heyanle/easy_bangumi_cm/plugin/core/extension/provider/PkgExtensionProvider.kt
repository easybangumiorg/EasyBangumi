package com.heyanle.easy_bangumi_cm.plugin.core.extension.provider

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.file_helper.JsonlFileHelper
import com.heyanle.easy_bangumi_cm.base.utils.jsonTo
import com.heyanle.easy_bangumi_cm.plugin.core.utils.FolderIndex
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import com.heyanle.easy_bangumi_cm.unifile.UniFileFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.File

/**
 * Created by heyanlin on 2024/12/17.
 */
class PkgExtensionProvider(
    workFolder: String,
    cacheFolder: String,
) : AbsExtensionProvider() {

    companion object {
        const val INDEX_FILE_NAME = "index.jsonl"
        const val FILE_SUFFIX = "eb.pkg"

        const val MANIFEST_FILE = "manifest.json"

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
    )


    // 用于异步加载
    private val singleDispatcher = CoroutineProvider.newSingle()
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

    private var lastJobType = 0
    private var lastJob: Job? = null

    override fun refresh() {
        scope.launch(singleDispatcher) {
            lastJob?.cancelAndJoin()
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
        val pkgFile = File(workFolderFile, "${indexItem.key}.$FILE_SUFFIX")
        val unzipFolder = File(workFolderFile, UNZIP_FOLDER)

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
                assetsPath = File(pkgFile.parentFile, ASSETS_FOLDER).absolutePath,
                workPath = unzipFolder.absolutePath,
                lastModified = pkgFile.lastModified(),
            )
        }

        // 2. 检查源文件是否有变化，如果有则需要重新安装
        val isChange = pkgFile.lastModified() != indexItem.lastModified || !unzipFolder.exists() || FolderIndex.check(unzipFolder.absolutePath, indexItem.lastModified)
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
        val manifestText = manifestFile.readText()
        val manifest = manifestText.jsonTo<PkgExtensionManifest>(ignoreError = true)
        if (manifest == null) {
            unzipFolder.deleteRecursively()
            return getErrorManifest(false, "Manifest 文件解析失败")
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
            providerType = ExtensionManifest.PROVIDER_TYPE_PKG,
            loadType = ExtensionManifest.LOAD_TYPE_JS_PKG,
            sourcePath = pkgFile.absolutePath,
            assetsPath = File(unzipFolder, ASSETS_FOLDER).absolutePath,
            workPath = unzipFolder.absolutePath,
            lastModified = pkgFile.lastModified(),
        )
    }

    override fun uninstall(extensionManifest: ExtensionManifest) {

    }

    override fun install(file: File, callback: ((ExtensionManifest?, Throwable?) -> Unit)?) {
        scope.launch(singleDispatcher) {
            lastJob?.join()
            lastJob = scope.launch {
                val installCache = File(cacheFolderFile, CACHE_INSTALL)
                installCache.deleteRecursively()
                installCache.mkdirs()
                try {
                    ZipFile(file).extractAll(installCache.absolutePath)
                } catch (e: ZipException) {
                    // 解压失败
                    callback?.invoke(null, e)
                    return@launch
                }

                val manifestFile = File(installCache, MANIFEST_FILE)
                if(!manifestFile.exists()){
                    // manifest 文件不存在
                    callback?.invoke(null, Throwable("Manifest 文件不存在"))
                    return@launch
                }

                val manifestText = manifestFile.readText()
                val manifest = manifestText.jsonTo<PkgExtensionManifest>(ignoreError = true)
                if(manifest == null){
                    // manifest 文件解析失败
                    callback?.invoke(null, Throwable("Manifest 文件解析失败"))
                    return@launch
                }

                val key = manifest.key
                val targetFile = File(workFolderFile, "$key.$FILE_SUFFIX")

            }
        }

    }

    override fun release() {
        TODO("Not yet implemented")
    }



}