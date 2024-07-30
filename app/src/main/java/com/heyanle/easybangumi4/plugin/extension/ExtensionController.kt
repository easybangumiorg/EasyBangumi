package com.heyanle.easybangumi4.plugin.extension

import android.content.Context
import com.heyanle.easybangumi4.crash.SourceCrashController
import com.heyanle.easybangumi4.plugin.extension.provider.FileApkExtensionProvider
import com.heyanle.easybangumi4.plugin.extension.provider.FileJsExtensionProvider
import com.heyanle.easybangumi4.plugin.extension.provider.InstalledAppExtensionProvider
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntime
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

/**
 * InstalledAppExtensionProvider    ↘
 * FileApkExtensionProvider         → ExtensionController
 * FileJsExtensionProvider          ↗
 * Created by heyanlin on 2023/10/24.
 */
class ExtensionController(
    private val context: Context,
    val apkFileExtensionFolder: String,
    val jsExtensionFolder: String,
    private val cacheFolder: String,
    //private val extensionLoader: ExtensionLoader
) {

    companion object {
        private const val TAG = "ExtensionController"

    }

    private val jsRuntimeProvider = JSRuntimeProvider(2)

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)


    data class ExtensionState(
        val loading: Boolean = true,
        val extensionInfoMap: Map<String, ExtensionInfo> = emptyMap()
    )
    private val _state = MutableStateFlow<ExtensionState>(
        ExtensionState()
    )
    val state = _state.asStateFlow()


    private val installedAppExtensionProvider: InstalledAppExtensionProvider by lazy {
        InstalledAppExtensionProvider(
            context,
            dispatcher
        )
    }

    private val fileApkExtensionProvider: FileApkExtensionProvider by lazy {
        FileApkExtensionProvider(
            context,
            apkFileExtensionFolder,
            dispatcher,
            cacheFolder
        )
    }

    private val fileJsExtensionProvider: FileJsExtensionProvider by lazy {
        FileJsExtensionProvider(
            jsRuntimeProvider,
            jsExtensionFolder,
            dispatcher,
            cacheFolder
        )
    }



    fun init() {
        SourceCrashController.onExtensionStart()
        installedAppExtensionProvider.init()
        fileApkExtensionProvider.init()
        fileJsExtensionProvider.init()
        SourceCrashController.onExtensionEnd()

        scope.launch {
            combine(
                installedAppExtensionProvider.flow,
                fileApkExtensionProvider.flow,
                fileJsExtensionProvider.flow
            ) { installedAppExtensionProviderState, fileApkExtensionProviderState, fileJsExtensionProviderState ->
                val map = mutableMapOf<String, ExtensionInfo>()
                installedAppExtensionProviderState.extensionMap.forEach {
                    map[it.key] = it.value
                }
                fileApkExtensionProviderState.extensionMap.forEach {
                    map[it.key] = it.value
                }
                fileJsExtensionProviderState.extensionMap.forEach {
                    map[it.key] = it.value
                }
                ExtensionState(
                    loading = installedAppExtensionProviderState.loading && fileApkExtensionProviderState.loading && fileJsExtensionProviderState.loading,
                    extensionInfoMap = map
                )
            }.collectLatest { ext ->
                _state.update {
                    ext
                }
            }
        }

    }


    fun appendExtensionPath(path: String, callback: ((Exception?) -> Unit)? = null) {
        scope.launch {
            try {

                val file = File(path)
                if (!file.exists() || !file.canRead()) {
                    callback?.invoke(IOException("文件不存在或无法读取"))
                    return@launch
                }

                if (file.name.endsWith(FileJsExtensionProvider.EXTENSION_SUFFIX)) {
                    fileJsExtensionProvider.appendExtensionPath(path)
                } else if (file.name.endsWith(FileApkExtensionProvider.EXTENSION_SUFFIX)) {
                    fileApkExtensionProvider.appendExtensionPath(path)
                } else {
                    callback?.invoke(IOException("不支持的文件类型"))
                }
                callback?.invoke(null)
            } catch (e: IOException) {
                e.printStackTrace()
                callback?.invoke(e)
            }
        }
    }




}