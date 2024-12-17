package com.heyanle.easy_bangumi_cm.plugin.core.extension.provider

import com.heyanle.easy_bangumi_cm.base.data.DataState
import com.heyanle.easy_bangumi_cm.base.utils.file_helper.JsonlFileHelper
import com.heyanle.easy_bangumi_cm.plugin.core.utils.FolderIndex
import com.heyanle.easy_bangumi_cm.unifile.UniFileFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import net.lingala.zip4j.ZipFile
import java.io.File

/**
 * Created by heyanlin on 2024/12/17.
 */
class PkgExtensionProvider(
    sourceFolder: String,
    workFolder: String,
    cacheFolder: String,
    // ！！这里需要单线程 Scope！！
    private val scope: CoroutineScope
) : AbsExtensionProvider() {

    companion object {
        const val INDEX_FILE_NAME = "index.jsonl"
        const val FILE_SUFFIX = "eb.pkg"

        const val MANIFEST_FILE = "manifest.json"
        const val ASSETS_FOLDER = "assets"
    }

    class PkgExtensionIndexItem(
        val fileName: String,
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

    private val sourceFolderFile: File by lazy { File(sourceFolder).apply { mkdirs() } }
    private val workFolderFile: File by lazy { File(workFolder).apply { mkdirs() } }
    private val workIndexFile: File by lazy { File(workFolder, INDEX_FILE_NAME) }

    private val workIndexHelper: JsonlFileHelper<PkgExtensionIndexItem> by lazy {
        JsonlFileHelper(
            folder = UniFileFactory.fromFile(sourceFolderFile),
            name = INDEX_FILE_NAME,
            type = PkgExtensionIndexItem::class.java
        )
    }

    private var loadJob: Job? = null


    init {
        scope.launch {
            workIndexHelper.flow.collectLatest {
                if (it is DataState.Ok) {
                    val oldJob = loadJob
                    loadJob = scope.launch {
                        oldJob?.cancelAndJoin()
                        innerLoadFromIndex(it.data)
                    }
                } else {
                    fireLoading(true)
                }
            }
        }
    }


    private var refreshJob: Job? = null
    override fun load() {
        TODO("Not yet implemented")
    }

    private suspend fun innerLoadFromIndex(list: List<PkgExtensionIndexItem>) {
        list.map {
            val sourceFile = File(sourceFolderFile, it.fileName)
            if (!sourceFile.exists()) {
                return@map null
            }
            val needConfirm = sourceFile.lastModified() != it.lastModified
            val folder = File(workFolderFile, it.fileName)
            val needUnzip = !folder.exists() || !FolderIndex.check(folder.absolutePath, it.lastModified)
            yield()
            return@map Triple(it.fileName, needConfirm, needUnzip)
        }.filterNotNull().map {
            val key = it.first
            val needUnzip = it.third
            val folder = File(workFolderFile, key)
            if (needUnzip) {
                folder.deleteRecursively()
                folder.mkdirs()
                ZipFile(File(sourceFolderFile, key)).extractAll(folder.absolutePath)
                yield()
            }
            val manifestFile = File(folder, MANIFEST_FILE)
            if (!manifestFile.exists()) {
                folder.deleteRecursively()
                return@map null
            }
        }
    }

    override fun release() {

    }


}