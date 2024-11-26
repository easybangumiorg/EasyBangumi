package com.heyanle.easybangumi4.plugin.extension.provider

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.loader.AppExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoaderFactory
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

/**
 * Created by heyanle on 2024/7/29.
 * https://github.com/heyanLE
 */
class InstalledAppExtensionProvider(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
): ExtensionProvider {

    companion object {
        const val TAG = "InstalledAppExtensionProvider"
    }

    private val _state = MutableStateFlow(
        ExtensionProvider.ExtensionProviderState(true, emptyMap())
    )
    private val state = _state.asStateFlow()

    private val extensionReceiver = ExtensionInstallReceiver()

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var lastScanAppJob: Job? = null

    fun scanApp() {
        lastScanAppJob?.cancel()
        lastScanAppJob = scope.launch {
            extensionReceiver.safeUnregister()
            TimeLogUtils.i("scanApp star")
            val extensionInfos = ExtensionLoaderFactory.getInstalledAppExtensionLoaders(
                context,
            ).filter {
                it.canLoad()
            }.map {
                it.load()
            }.filterIsInstance<ExtensionInfo>()
            extensionInfos.forEach {
                it.logi(TAG)
            }
            _state.update {
                ExtensionProvider.ExtensionProviderState(false, extensionInfos.associateBy { it.key })
            }
            extensionReceiver.register()
        }
    }

    override val flow: StateFlow<ExtensionProvider.ExtensionProviderState>
        get() = state

    override fun init() {
        scanApp()
    }

    override fun release() {
        extensionReceiver.safeUnregister()

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

        fun safeUnregister() {
            try {
                context.unregisterReceiver(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        override fun onReceive(con: Context?, intent: Intent?) {
            val context = con ?: return
            when (intent?.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    getPackageNameFromIntent(intent)?.let {
                        // 安装，如果安装的是拓展则刷新一波
                        try {
                            if (AppExtensionLoader(context, it).canLoad()) {
                                scanApp()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                Intent.ACTION_PACKAGE_REPLACED -> {
                    getPackageNameFromIntent(intent)?.let {
                        // 升级
                        if (state.value.extensionMap.containsKey("app:${it}")) {
                            scanApp()
                        }
                    }
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    getPackageNameFromIntent(intent)?.let {
                        // 卸载
                        if (state.value.extensionMap.containsKey("app:${it}")) {
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