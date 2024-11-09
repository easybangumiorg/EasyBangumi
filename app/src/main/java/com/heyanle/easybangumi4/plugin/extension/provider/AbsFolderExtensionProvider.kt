package com.heyanle.easybangumi4.plugin.extension.provider

import android.os.Build
import android.os.FileObserver
import androidx.annotation.RequiresApi
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.utils.TimeLogUtils
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by heyanle on 2024/7/29.
 * https://github.com/heyanLE
 */
abstract class AbsFolderExtensionProvider(
    protected val folderPath: String,
    protected val cacheFolder: String,
    protected val dispatcher: CoroutineDispatcher,
): ExtensionProvider {

    protected val _state = MutableStateFlow(
        ExtensionProvider.ExtensionProviderState(true, emptyMap())
    )
    protected val state = _state.asStateFlow()

    protected val fileObserver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ExtensionFolderObserverQ(folderPath)
    } else {
        ExtensionFolderObserver(folderPath)
    }

    protected val scope = CoroutineScope(SupervisorJob() + dispatcher)
    protected var lastScanFolderJob: Job? = null

    protected val atomicLong = AtomicLong(0)

    protected val cacheFolderFile = File(cacheFolder)

    fun scanFolder() {
        lastScanFolderJob?.cancel()
        lastScanFolderJob = scope.launch {
            TimeLogUtils.i("scanFolder star")
            // 先关闭监听
            fileObserver.stopWatching()

            val file = File(folderPath)
            val fileList = if (file.exists() && file.isDirectory) {
                file.listFiles()?.filter {
                    it != null && it.isFile && checkName(it.name)
                }
            }else {
                emptyList()
            } ?: emptyList()


            val extensionInfos = coverExtensionLoaderList(loadExtensionLoader(fileList)).map {
                it.load()
            }.filterIsInstance<ExtensionInfo>()


            _state.update {
                ExtensionProvider.ExtensionProviderState(
                    false,
                    extensionInfos.associateBy { it.key })
            }
            fileObserver.startWatching()
        }
    }

    abstract fun checkName(displayName: String): Boolean
    abstract fun getNameWhenLoad(displayName: String, time: Long, atomicLong: Long): String

    abstract fun loadExtensionLoader(fileList: List<File>): List<ExtensionLoader>

    open fun coverExtensionLoaderList(loaderList: List<ExtensionLoader>): List<ExtensionLoader> {
        return loaderList;
    }

    fun stopWatching() {
        fileObserver.stopWatching()
    }

    fun startWatching() {
        fileObserver.startWatching()
    }

    fun appendExtensionPath(path: String, callback: ((Exception?) -> Unit)? = null) {
        scope.launch {
            try {
                val inputStream = File(path).inputStream()
                innerAppendExtension(path, inputStream)
                callback?.invoke(null)
            } catch (e: IOException) {
                e.printStackTrace()
                callback?.invoke(e)
            }
        }
    }

    fun appendExtensionStream(displayName: String, inputStream: InputStream, callback: ((Exception?) -> Unit)? = null) {
        scope.launch {
            try {
                innerAppendExtension(displayName, inputStream)
                callback?.invoke(null)
            } catch (e: IOException) {
                e.printStackTrace()
                callback?.invoke(e)
            }
        }
    }

    /**
     * 调用完后 inputSteam 会自动 close
     */
    protected fun innerAppendExtension(displayName: String, inputStream: InputStream) {
        fileObserver.stopWatching()
        val fileName = getNameWhenLoad(displayName, System.currentTimeMillis(), atomicLong.getAndIncrement())
            // "${System.currentTimeMillis()}-${atomicLong.getAndIncrement()}${getSuffix()}"
        val cacheFile = File(cacheFolder, fileName)
        val targetFile = File(folderPath, fileName)
        val targetFileTemp = File(folderPath, "${fileName}.temp")
        cacheFolderFile.mkdirs()
        cacheFile.createNewFile()
        inputStream.use { input ->
            cacheFile.outputStream().use { out ->
                input.copyTo(out)
            }
        }
        cacheFile.deleteOnExit()
        val loader = loadExtensionLoader(listOf(cacheFile)).firstOrNull() ?: return
        if (loader.canLoad()) {
            cacheFile.copyTo(targetFileTemp)
            targetFileTemp.renameTo(targetFile)
        }
        cacheFolderFile.deleteRecursively()
        cacheFolderFile.mkdirs()
        scanFolder()
        fileObserver.startWatching()
    }


    override val flow: StateFlow<ExtensionProvider.ExtensionProviderState>
        get() = state

    override fun init() {
        scanFolder()
    }

    override fun release() {
        fileObserver.stopWatching()
    }

    // 文件观察

    protected fun onEvent(event: Int, path: String) {
        if (event and FileObserver.DELETE == FileObserver.DELETE || event and FileObserver.DELETE_SELF == FileObserver.DELETE_SELF || checkName(path)) {
            scanFolder()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    inner class ExtensionFolderObserverQ(protected val extensionFolder: String) :
        FileObserver(
            File(extensionFolder),
            DELETE_SELF or DELETE
        ) {

        override fun onEvent(event: Int, path: String?) {
            "${event} ${path} onEvent".logi(FileApkExtensionProvider.TAG)
            if (event and DELETE == DELETE || event and DELETE_SELF == DELETE_SELF || path != null) {
                this@AbsFolderExtensionProvider.onEvent(event, path ?: "")
            }
        }
    }

    inner class ExtensionFolderObserver(protected val extensionFolder: String) :
        FileObserver(
            extensionFolder,
            DELETE_SELF or DELETE
        ) {
        override fun onEvent(event: Int, path: String?) {
            "${event} ${path} onEvent".logi(FileApkExtensionProvider.TAG)
            if (event and DELETE == DELETE || event and DELETE_SELF == DELETE_SELF || path != null) {
                this@AbsFolderExtensionProvider.onEvent(event, path ?: "")
            }
        }

    }

}