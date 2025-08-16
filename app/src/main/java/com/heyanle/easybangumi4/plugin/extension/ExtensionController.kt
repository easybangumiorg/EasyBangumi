package com.heyanle.easybangumi4.plugin.extension

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.crash.SourceCrashController
import com.heyanle.easybangumi4.plugin.extension.provider.FileApkExtensionProvider
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProvider
import com.heyanle.easybangumi4.plugin.extension.provider.InstalledAppExtensionProvider
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import com.hippo.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
): IExtensionController {

    companion object {
        private const val TAG = "ExtensionController"

    }

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)



    private val _state = MutableStateFlow<IExtensionController.ExtensionState>(
        IExtensionController.ExtensionState()
    )
    override val state = _state.asStateFlow()

    private var firstLoad = true

    // ============================ 插件 Provider ============================

    // 已安装 apk
    private val installedAppExtensionProvider: InstalledAppExtensionProvider by lazy {
        InstalledAppExtensionProvider(
            context,
            dispatcher
        )
    }

    // 文件夹里的 apk
    private val fileApkExtensionProvider: FileApkExtensionProvider by lazy {
        FileApkExtensionProvider(
            context,
            apkFileExtensionFolder,
            dispatcher,
            cacheFolder
        )
    }

    // js 文件
    private val jsRuntimeProvider = JSRuntimeProvider(2)
    private val jsExtensionProvider: JsExtensionProvider by lazy {
        JsExtensionProvider(
            jsRuntimeProvider,
            jsExtensionFolder,
            dispatcher,
            cacheFolder
        )
    }



    override fun init() {
        SourceCrashController.onExtensionStart()
        installedAppExtensionProvider.init()
        fileApkExtensionProvider.init()
        jsExtensionProvider.init()
        SourceCrashController.onExtensionEnd()

        scope.launch {
            combine(
                installedAppExtensionProvider.flow,
                fileApkExtensionProvider.flow,
                jsExtensionProvider.flow
            ) { installedAppExtensionProviderState, fileApkExtensionProviderState, fileJsExtensionProviderState ->
                // 首次必须所有 Provider 都加载完才算加载完
                if (firstLoad &&
                    (installedAppExtensionProviderState.loading || fileApkExtensionProviderState.loading || fileJsExtensionProviderState.loading)) {
                    return@combine IExtensionController.ExtensionState(
                        loading = true,
                        extensionInfoMap = emptyMap()
                    )
                }
                firstLoad = false
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
                IExtensionController.ExtensionState(
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

    suspend fun <R> withNoWatching(isScan: Boolean = true, block:suspend  ()-> R): R? {
        jsExtensionProvider.stopWatching()
        fileApkExtensionProvider.stopWatching()
        val r = try {
            block()
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
        jsExtensionProvider.startWatching()
        fileApkExtensionProvider.startWatching()

        if (isScan) {
            jsExtensionProvider.scanFolder()
            fileApkExtensionProvider.scanFolder()
        }
        return r
    }


    // 如果 type 没指定，会根据文件后缀名判断
    suspend fun appendExtensionUri(uri: Uri, type: Int = -1) : Exception? {
        return withContext(dispatcher) {
            try {
                val uniFile = UniFile.fromUri(context, uri)
                if (uniFile?.exists() != true || !uniFile.canRead()){
                    return@withContext IOException("文件不存在或无法读取")
                }

                val name = uniFile.name ?: ""
                if (JsExtensionProvider.isEndWithJsExtensionSuffix(name) || type == ExtensionInfo.TYPE_JS_FILE) {
                    jsExtensionProvider.appendExtensionStream(name, uniFile.openInputStream())
                } else if (name.endsWith(FileApkExtensionProvider.EXTENSION_SUFFIX) || type == ExtensionInfo.TYPE_APK_FILE) {
                    fileApkExtensionProvider.appendExtensionStream(name, uniFile.openInputStream())
                } else {
                    return@withContext IOException("不支持的文件类型")
                }
                return@withContext null
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext e
            }
        }
    }

    suspend fun appendExtensionFile(file: File, type: Int = -1) : Exception? {
        return withContext(dispatcher) {
            try {
                if (!file.exists() || !file.canRead()){
                    return@withContext IOException("文件不存在或无法读取")
                }

                val name = file.name ?: ""
                if (JsExtensionProvider.isEndWithJsExtensionSuffix(name) || type == ExtensionInfo.TYPE_JS_FILE) {
                    jsExtensionProvider.appendExtensionStream(name, file.inputStream())
                } else if (name.endsWith(FileApkExtensionProvider.EXTENSION_SUFFIX) || type == ExtensionInfo.TYPE_APK_FILE) {
                    fileApkExtensionProvider.appendExtensionStream(name, file.inputStream())
                } else {
                    return@withContext IOException("不支持的文件类型")
                }
                return@withContext null
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext e
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

                if (JsExtensionProvider.isEndWithJsExtensionSuffix(file.name)) {
                    jsExtensionProvider.appendExtensionPath(path)
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