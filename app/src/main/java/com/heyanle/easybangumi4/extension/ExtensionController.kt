package com.heyanle.easybangumi4.extension

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.FileObserver
import androidx.annotation.RequiresApi
import com.heyanle.easybangumi4.extension.loader.AppExtensionLoader
import com.heyanle.easybangumi4.extension.loader.ExtensionLoaderFactory
import com.heyanle.easybangumi4.extension.loader.FileExtensionLoader
import com.heyanle.easybangumi4.utils.TimeLogUtils
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.extension_api.iconFactory
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * 安装的拓展                    --→ ExtensionState
 * extensionFolder 文件夹下的拓展 -↗
 * 注：
 * 1. 对于文件拓展，如果同一个拓展在文件夹里有两个文件，那么在这里依然识别为两个拓展，具体去重由上层 Source 实现
 * 2. 如果一个拓展同时存在于文件和安装，则在这里依然会识别为两个拓展，具体去重由上层 Source 实现
 * 3. 该组件会持续观察 extensionFolder 文件夹，直接在里面添加文件即可，后缀为 .easybangumi.apk
 * 4. 该组件会接受软件 安装-卸载-升级广播
 * Created by heyanlin on 2023/10/24.
 */
class ExtensionController(
    private val context: Context,
    val extensionFolder: String,
    private val cacheFolder: String,
    //private val extensionLoader: ExtensionLoader
) {

    companion object {
        private const val TAG = "ExtensionController"
        // app 内部下载文件的特殊后缀
        const val EXTENSION_SUFFIX = ".easybangumi.apk"
    }

    data class ExtensionState(
        val isFileExtensionLoading: Boolean = true,
        val isAppExtensionLoading: Boolean = true,
        val fileExtension: Map<String, Extension> = emptyMap(),
        val appExtensions: Map<String, Extension> = emptyMap(),
    ){
        val isLoading: Boolean
            get() = isAppExtensionLoading || isFileExtensionLoading
    }

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var lastScanFolderJob: Job? = null
    private var lastScanAppJob: Job? = null

    private val _state = MutableStateFlow<ExtensionState>(ExtensionState())
    val state = _state.asStateFlow()

    private val fileObserver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ExtensionFolderObserverQ(extensionFolder)
    } else ExtensionFolderObserver(extensionFolder)

    private val extensionReceiver = ExtensionInstallReceiver()

    private val atomicLong = AtomicLong(0)

    private val cacheFolderFile = File(cacheFolder)


    fun init() {
        // 只有第一次需要提示加载中
        _state.update {
            ExtensionState(true, true)
        }
        scanFolder()
        scanApp()
    }

    fun scanFolder() {
        lastScanFolderJob?.cancel()
        lastScanFolderJob = scope.launch {
            TimeLogUtils.i("scanFolder star")
            // 先关闭监听
            fileObserver.stopWatching()
            val extensions = ExtensionLoaderFactory.getFileExtensionLoaders(
                context,
                extensionFolder
            ).map {
                it.load()
            }.filterIsInstance<Extension>()
            updateExtensions(extensions, false)
            fileObserver.startWatching()
        }
    }

    fun scanApp() {
        lastScanAppJob?.cancel()
        lastScanAppJob = scope.launch {
            extensionReceiver.safeUnregister()
            TimeLogUtils.i("scanApp star")
            val extensions = ExtensionLoaderFactory.getAppExtensionLoaders(
                context,
            ).filter {
                it.canLoad()
            }.map {
                it.load()
            }.filterIsInstance<Extension>()
            extensions.forEach {
                it.logi(TAG)
            }
            updateExtensions(extensions, true)
            extensionReceiver.register()
        }
    }

    private fun updateExtensions(extensions: Collection<Extension>, isApp: Boolean) {
        TimeLogUtils.i("updateExtension isApp: ${isApp}")
        extensions.forEach {
            it.logi(TAG)
        }
        _state.update { state ->
            val map = hashMapOf<String, Extension>()

            extensions.forEach {
                val old = map[it.key]
                var new = it
                if (old != null && new.versionCode < old.versionCode) {
                    new = old
                }
                map[it.key] = new
            }
            state.copy(
                isFileExtensionLoading = if(isApp) state.isFileExtensionLoading else false,
                isAppExtensionLoading = if(isApp) false else state.isAppExtensionLoading,
                appExtensions = if (isApp) map else state.appExtensions,
                fileExtension = if (isApp) state.fileExtension else map,
            )
        }
    }

    /**
     * 将 uri 复制到 extensionFolder 然后会触发 FileObserver 加载
     */
    fun appendExtensionUri(uri: String, callback: ((Exception?) -> Unit)? = null) {
        scope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(uri))
                if (inputStream == null) {
                    callback?.invoke(IllegalArgumentException("uri parse error"))
                    return@launch
                }
                innerAppendExtension(inputStream)
                callback?.invoke(null)
            } catch (e: IOException) {
                e.printStackTrace()
                callback?.invoke(e)
            }
        }
    }

    fun appendExtensionPath(path: String, callback: ((Exception?) -> Unit)? = null) {
        scope.launch {
            try {
                val inputStream = File(path).inputStream()
                innerAppendExtension(inputStream)
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
    private fun innerAppendExtension(inputStream: InputStream) {
        val fileName =
            "${System.currentTimeMillis()}-${atomicLong.getAndIncrement()}${EXTENSION_SUFFIX}"
        val cacheFile = File(cacheFolder, fileName)
        val targetFile = File(extensionFolder, fileName)
        val targetFileTemp = File(extensionFolder, "${fileName}.temp")
        cacheFolderFile.mkdirs()
        cacheFile.createNewFile()
        inputStream.use { input ->
            cacheFile.outputStream().use { out ->
                input.copyTo(out)
            }
        }
        val loader = FileExtensionLoader(context, cacheFile.absolutePath)
        if (loader.canLoad()) {
            cacheFile.copyTo(targetFileTemp)
            targetFileTemp.renameTo(targetFile)
        }
        cacheFolderFile.deleteRecursively()
        cacheFolderFile.mkdirs()
    }

    /**
     * 如果是文件加载就删除文件，如果是 app 加载就跳转到卸载按钮
     */
    fun removeExtension(extension: Extension) {

    }


    // ======== 文件夹监听

    private fun onEvent(event: Int, path: String) {
        if (path.endsWith(EXTENSION_SUFFIX)) {
            scanFolder()
        }
    }


    // 观察文件夹
    @RequiresApi(Build.VERSION_CODES.Q)
    inner class ExtensionFolderObserverQ(private val extensionFolder: String) :
        FileObserver(
            File(extensionFolder),
            MODIFY or CLOSE_WRITE or DELETE_SELF or MOVED_TO or MOVED_FROM or MOVE_SELF
        ) {

        override fun onEvent(event: Int, path: String?) {
            path?.let {
                this@ExtensionController.onEvent(event, it)
            }
        }
    }

    inner class ExtensionFolderObserver(private val extensionFolder: String) :
        FileObserver(
            extensionFolder,
            MODIFY or CLOSE_WRITE or DELETE_SELF or MOVED_TO or MOVED_FROM or MOVE_SELF
        ) {
        override fun onEvent(event: Int, path: String?) {
            path?.let {
                this@ExtensionController.onEvent(event, it)
            }
        }

    }

    // ======== app 监听

    inner class ExtensionInstallReceiver : BroadcastReceiver() {

        private val filter
            get() = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addDataScheme("package")
            }

        fun register() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(this, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(this, filter)
            }
        }

        fun safeUnregister(){
            try {
                context.unregisterReceiver(this)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        fun unregister() {

            context.unregisterReceiver(this)
        }

        override fun onReceive(con: Context?, intent: Intent?) {
            val context = con ?: return
            when (intent?.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    getPackageNameFromIntent(intent)?.let {
                        // 安装，如果安装的是拓展则刷新一波
                        if(AppExtensionLoader(context, it).canLoad()){
                            scanApp()
                        }
                    }
                }

                Intent.ACTION_PACKAGE_REPLACED -> {
                    getPackageNameFromIntent(intent)?.let {
                        // 升级
                        if (state.value.appExtensions.containsKey("app:${it}")) {
                            scanApp()
                        }
                    }
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    getPackageNameFromIntent(intent)?.let {
                        // 卸载
                        if (state.value.appExtensions.containsKey("app:${it}")) {
                            scanApp()
                        }
                    }
                }
            }
        }

        private fun getPackageNameFromIntent(intent: Intent?): String? {
            return intent?.data?.encodedSchemeSpecificPart ?: return null
        }


    }

}