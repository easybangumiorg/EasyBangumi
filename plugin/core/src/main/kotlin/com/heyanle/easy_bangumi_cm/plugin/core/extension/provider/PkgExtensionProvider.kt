package com.heyanle.easy_bangumi_cm.plugin.core.extension.provider

import com.heyanle.easy_bangumi_cm.base.utils.jsonTo
import com.heyanle.easy_bangumi_cm.plugin.core.utils.FolderIndex
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import net.lingala.zip4j.ZipFile
import java.io.File

/**
 * Created by heyanlin on 2024/12/13.
 */
class PkgExtensionProvider(
    sourceFolder: String,
    workFolder: String,
    cacheFolder: String,
    // ！！这里需要单线程 Scope！！
    private val singleScope: CoroutineScope
) : ExtensionProvider {

    companion object {
        const val FILE_SUFFIX = "eb.pkg"
        const val MANIFEST_FILE = "manifest.json"
        const val ASSETS_FOLDER = "assets"
    }

    class PkgExtensionManifest(
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

    private val _flow =
        MutableStateFlow<ExtensionProvider.ExtensionProviderState>(ExtensionProvider.ExtensionProviderState())

    override val flow: StateFlow<ExtensionProvider.ExtensionProviderState>
        get() = _flow

    private var loadJob: Job? = null

    private val sourceFolderFile: File by lazy {
        File(sourceFolder).apply {
            mkdirs()
        }
    }

    private val workFolderFile: File by lazy {
        File(workFolder).apply {
            mkdirs()
        }
    }

    override fun load() {
        loadJob?.cancel()
        loadJob = singleScope.launch {
            _flow.update {
                it.copy(
                    loading = false,
                    extensionManifestMap = innerLoad(it.extensionManifestMap)
                )
            }
        }

    }

    /**
     * 1. 自动处理 key 冲突
     * 2. 自动处理文件名不为 key
     * 3. 缓存解压
     */
    private suspend fun innerLoad(
        o: Map<String, ExtensionManifest>,
    ): Map<String, ExtensionManifest> {
        val listFile = sourceFolderFile.listFiles() ?: emptyArray()
        val idsMap = HashSet<String>()
        idsMap.addAll(o.keys)
        for (file in listFile) {
            if (file == null) {
                continue
            }
            val name = file.name
            if (name.endsWith(".$FILE_SUFFIX")) {
                val id = name.substring(0, name.length - FILE_SUFFIX.length - 1)
                idsMap.add(id)
            }
        }

        val res = hashMapOf<String, ExtensionManifest>()

        val deferredList = mutableListOf<Deferred<ExtensionManifest?>>()

        for (s in idsMap) {
            val sourceFile = File(sourceFolderFile, "$s.$FILE_SUFFIX")

            val cacheFolder = File(workFolderFile, s)

            // or 删除缓存
            if (!sourceFile.exists()) {
                val deferred = singleScope.async {
                    cacheFolder.delete()
                    null
                }
                deferredList.add(deferred)
                continue
            }

            // or 直接走缓存
            if (cacheFolder.exists() && FolderIndex.check(cacheFolder.absolutePath, sourceFile.lastModified())) {
                continue
            }

            // or 解压 Source
            if (!cacheFolder.exists() || FolderIndex.check(cacheFolder.absolutePath, sourceFile.lastModified())) {
                val deferred = singleScope.async {
                    val unzipFolder = File(workFolderFile, s)

                    unzipFolder.deleteRecursively()
                    unzipFolder.mkdirs()

                    // 解压
                    ZipFile(sourceFile).extractAll(unzipFolder.absolutePath)

                    // 读取 manifest
                    val manifestFile = File(unzipFolder, MANIFEST_FILE)
                    if (!manifestFile.exists()) {
                        unzipFolder.deleteRecursively()
                        return@async null
                    }

                    val manifestText = manifestFile.readText()
                    val manifest = manifestText.jsonTo<PkgExtensionManifest>(ignoreError = true)
                    if (manifest == null) {
                        unzipFolder.deleteRecursively()
                        return@async null
                    }

                    // 生成 ExtensionManifest
                    ExtensionManifest(
                        key = manifest.key,
                        label = manifest.label,
                        versionCode = manifest.versionCode,
                        libVersion = manifest.libVersion,
                        readme = manifest.readme,
                        author = manifest.author,
                        icon = manifest.icon,
                        loadType = ExtensionManifest.LOAD_TYPE_JS_PKG,
                        sourcePath = sourceFile.absolutePath,
                        assetsPath = File(unzipFolder, ASSETS_FOLDER).absolutePath,
                        folderPath = unzipFolder.absolutePath,
                        lastModified = sourceFile.lastModified(),
                        ext = manifest.key == s
                    )
                }
                deferredList.add(deferred)
                continue
            }


        }


        val temp = hashMapOf<String, List<ExtensionManifest>>()
        for (deferred in deferredList) {
            val manifest = deferred.await()
            if (manifest != null) {
                temp[manifest.key] = temp[manifest.key]?.let {
                    it + manifest
                } ?: listOf(manifest)
            }
        }

        // key 冲突或者文件名不为 id.pkg 的
        val conflict = hashMapOf<String, List<ExtensionManifest>>()
        val needRename = arrayListOf<ExtensionManifest>()
        for (mutableEntry in temp) {
            if (mutableEntry.value.isEmpty()) {
                continue
            } else if (mutableEntry.value.size > 1) {
                // key 冲突需要解决
                conflict[mutableEntry.key] = mutableEntry.value
            } else {
                val manifest = mutableEntry.value[0]
                val id = manifest.ext as? String ?: continue
                if (id == mutableEntry.key) {
                    // 正常加载
                    res[mutableEntry.key] = manifest
                } else {
                    // key 和文件名不同需要重命名
                    needRename.add(manifest)
                }
            }
        }

        for (mutableEntry in conflict) {
            val list = mutableEntry.value
            // 取最高版本
            val max = list.maxByOrNull { it.versionCode } ?: continue
            list.forEach {
                if (it != max) {
                    File(it.folderPath).deleteRecursively()
                }
            }
            if ((max.ext as? String ?: continue) == mutableEntry.key) {
                res[mutableEntry.key] = max
            } else {
                // 需要重命名
                // key 和文件名不同需要重命名
                needRename.add(max)
            }
        }

        for (manifest in needRename) {
            // rename source
            val sourceFile = File(manifest.sourcePath ?: continue)
            val newFile = File(sourceFolderFile, "${manifest.key}.$FILE_SUFFIX")
            sourceFile.renameTo(newFile)

            // rename folder
            val folder = File(manifest.folderPath)
            val newFolder = File(workFolderFile, manifest.key)
            newFolder.deleteRecursively()
            newFolder.mkdirs()
            folder.copyRecursively(newFolder, true)

            // update manifest
            val newManifest = manifest.copy(
                key = manifest.key,
                sourcePath = newFile.absolutePath,
                folderPath = newFolder.absolutePath,
                assetsPath = File(newFolder, ASSETS_FOLDER).absolutePath,
                lastModified = newFile.lastModified(),
            )

            res[newManifest.key] = newManifest
        }

        // 删除多余文件夹
        workFolderFile.listFiles()?.forEach {
            if (it.isDirectory && !res.containsKey(it.name)) {
                it.deleteRecursively()
            }
        }

        // index
        for (re in res) {
            FolderIndex.make(re.value.folderPath, re.value.lastModified)
        }

        return res
    }

    override fun release() {
        loadJob?.cancel()
        _flow.update {
            ExtensionProvider.ExtensionProviderState()
        }
    }
}