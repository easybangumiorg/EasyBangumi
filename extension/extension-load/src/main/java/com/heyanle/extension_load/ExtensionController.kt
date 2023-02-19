package com.heyanle.extension_load

import android.content.Context
import android.graphics.drawable.Drawable
import com.heyanle.extension_load.model.Extension
import com.heyanle.extension_load.model.LoadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Created by HeYanLe on 2023/2/19 16:14.
 * https://github.com/heyanLE
 */
class ExtensionController(
    private val context: Context
) {

    sealed class ExtensionState {
        object Loading : ExtensionState()

        class Extensions(val extensions: List<Extension>) : ExtensionState()
    }

    private val iconMap = mutableMapOf<String, Drawable>()

    private val _installedExtensionsFlow = MutableStateFlow<ExtensionState>(ExtensionState.Loading)
    val installedExtensionsFlow = _installedExtensionsFlow.asStateFlow()

    private val fileExtensionFile =
        context.getExternalFilesDir("extension") ?: File(context.filesDir, "extension")

    private val loadScope = MainScope()
    private var lastJob: Job? = null

    init {
        loadExtension()
        ExtensionInstallReceiver(InstallationListener()).register(context)
    }

    fun getAppIconForSource(sourceKey: String): Drawable? {
        val curState = (_installedExtensionsFlow.value as? ExtensionState.Extensions) ?: return null
        val pkgName =
            curState.extensions.find { ext -> ext.sources.any { it.key == sourceKey } }?.pkgName
        if (pkgName != null) {
            return iconMap[pkgName]
                ?: iconMap.getOrPut(pkgName) { context.packageManager.getApplicationIcon(pkgName) }
        }
        return null
    }

    fun loadExtension() {
        lastJob?.cancel()
        lastJob = loadScope.launch() {
            _installedExtensionsFlow.emit(ExtensionState.Loading)
            // 版本控制，取高版本的
            val extensionVersionMap = hashMapOf<String, Long>()
            val extensions = arrayListOf<Extension>()

            withContext(Dispatchers.IO) {
                // 加载安装成 app 的
                ExtensionLoader.loadExtensions(context).filterIsInstance<LoadResult.Success>()
                    .forEach {
                        if (it.extension.versionCode > (extensionVersionMap[it.extension.pkgName]
                                ?: -1L)
                        ) {
                            extensions.add(it.extension)
                            extensionVersionMap[it.extension.pkgName] = it.extension.versionCode
                        }

                    }

//                // 加载文件的
//                fileExtensionFile.mkdirs()
//                fileExtensionFile.listFiles()?.filter { it != null && it.name.endsWith(".apk") }
//                    ?.map {
//                        ExtensionLoader.loadExtensionByFile(context, it.absolutePath)
//                    }?.filterIsInstance<LoadResult.Success>()?.forEach {
//                        if (it.extension.versionCode > (extensionVersionMap[it.extension.pkgName]
//                                ?: -1L)
//                        ) {
//                            extensions.add(it.extension)
//                            extensionVersionMap[it.extension.pkgName] = it.extension.versionCode
//                        }
//                    }
            }
            _installedExtensionsFlow.emit(ExtensionState.Extensions(extensions))
        }
    }

    fun unregisterExtension(pkgName: String){
        loadScope.launch {
            lastJob?.join()
            val curState = (_installedExtensionsFlow.value as? ExtensionState.Extensions) ?: return@launch
            val oldExtensions = curState.extensions.toMutableList()
            val installedExtension = curState.extensions.find { it.pkgName == pkgName }
            if (installedExtension != null) {
                oldExtensions -= installedExtension
                _installedExtensionsFlow.emit(ExtensionState.Extensions(oldExtensions))
            }
        }
    }

    fun registerExtension(extension: Extension){
        loadScope.launch {
            lastJob?.join()
            val curState = (_installedExtensionsFlow.value as? ExtensionState.Extensions) ?: return@launch
            val oldExtensions = curState.extensions.toMutableList()
            val installedExtension = curState.extensions.find { it.pkgName == extension.pkgName }
            if (installedExtension == null || installedExtension.versionCode < extension.versionCode) {
                oldExtensions += extension
                _installedExtensionsFlow.emit(ExtensionState.Extensions(oldExtensions))
            }
        }
    }

    /**
     * Listener which receives events of the extensions being installed, updated or removed.
     */
    private inner class InstallationListener : ExtensionInstallReceiver.Listener {

        override fun onExtensionInstalled(extension: Extension) {
            registerExtension(extension)
        }

        override fun onExtensionUpdated(extension: Extension) {
            registerExtension(extension)
        }


        override fun onPackageUninstalled(pkgName: String) {
            unregisterExtension(pkgName)
        }
    }

}