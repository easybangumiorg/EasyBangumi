package com.heyanle.extension_load

import android.content.Context
import com.heyanle.extension_api.iconFactory
import com.heyanle.extension_load.model.Extension
import com.heyanle.extension_load.model.LoadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by HeYanLe on 2023/2/21 22:22.
 * https://github.com/heyanLE
 */
object ExtensionController {

    init {
        iconFactory = IconFactoryImpl()
    }

    sealed class ExtensionState {

        object None : ExtensionState()

        object Loading : ExtensionState()

        class Extensions(
            val extensions: List<Extension>
        ) : ExtensionState()
    }

    private val loadingExtensionLabel = MutableStateFlow<String?>(null)

    private val receiverInit = AtomicBoolean(false)

    private val _installedExtensionsFlow = MutableStateFlow<ExtensionState>(
        ExtensionState.None
    )
    val installedExtensionsFlow = _installedExtensionsFlow.asStateFlow()

    private val loadScope = MainScope()
    private var lastJob: Job? = null

    fun init(context: Context, loadPkgName: List<String>) {
        if (receiverInit.compareAndSet(false, true)) {
            ExtensionInstallReceiver(ReceiverListener()).register(context)
        }
        lastJob?.cancel()
        lastJob = loadScope.launch() {
            _installedExtensionsFlow.emit(ExtensionState.Loading)
            val extensions = withContext(Dispatchers.IO) {
                ExtensionLoader.getAllExtension(context, loadPkgName)
            }
            _installedExtensionsFlow.emit(ExtensionState.Extensions(extensions))
        }


    }

    fun load(context: Context, extension: Extension.Available) {
        loadScope.launch {
            val lastState =
                (_installedExtensionsFlow.value as? ExtensionState.Extensions) ?: return@launch
            if (loadingExtensionLabel.compareAndSet(null, extension.label)) {
                // cas 成功才加载
                val oldList = lastState.extensions.toMutableList()
                oldList.remove(extension)
                oldList.add(ExtensionLoader.loadExtension(context, extension))
                _installedExtensionsFlow.emit(ExtensionState.Extensions(oldList))
                loadingExtensionLabel.emit(null)
            } else {
                // 有扩展正在加载，什么都不做
                return@launch
            }
        }
    }

    class ReceiverListener : ExtensionInstallReceiver.Listener {
        override fun onExtensionInstalled(context: Context, pkgName: String) {
            loadScope.launch {
                if (loadingExtensionLabel.value != null) {
                    val job = loadScope.launch {
                        installedExtensionsFlow.collectLatest {
                            if (it is ExtensionState.Extensions) {
                                val oldList = it.extensions.toMutableList()
                                _installedExtensionsFlow.emit(ExtensionState.Loading)
                                (ExtensionLoader.getExtension(
                                    context,
                                    pkgName
                                ) as? LoadResult.Success)?.let {
                                    oldList.add(it.extension)
                                }
                                _installedExtensionsFlow.emit(ExtensionState.Extensions(oldList))
                                cancel()
                            }
                        }

                    }
                    // 超时五秒钟
                    delay(5000)
                    job.cancel()
                } else {
                    val lastState = _installedExtensionsFlow.value
                    if (lastState is ExtensionState.Extensions) {
                        val oldList = lastState.extensions.toMutableList()
                        _installedExtensionsFlow.emit(ExtensionState.Loading)
                        (ExtensionLoader.getExtension(
                            context,
                            pkgName
                        ) as? LoadResult.Success)?.let {
                            oldList.add(it.extension)
                        }
                        _installedExtensionsFlow.emit(ExtensionState.Extensions(oldList))
                    }
                }

            }
        }

        override fun onExtensionUpdated(context: Context, pkgName: String) {
            loadScope.launch {
                if (loadingExtensionLabel.value != null) {
                    val job = loadScope.launch {
                        installedExtensionsFlow.collectLatest {
                            if (it is ExtensionState.Extensions) {
                                val oldList = it.extensions.toMutableList()
                                _installedExtensionsFlow.emit(ExtensionState.Loading)
                                (ExtensionLoader.getExtension(
                                    context,
                                    pkgName
                                ) as? LoadResult.Success)?.let {
                                    oldList.add(it.extension)
                                }
                                _installedExtensionsFlow.emit(ExtensionState.Extensions(oldList))
                                cancel()
                            }
                        }

                    }
                    // 超时五秒钟
                    delay(5000)
                    job.cancel()
                } else {
                    val lastState = _installedExtensionsFlow.value
                    if (lastState is ExtensionState.Extensions) {
                        val oldList = lastState.extensions.toMutableList()
                        _installedExtensionsFlow.emit(ExtensionState.Loading)
                        oldList.find { it.pkgName == pkgName }?.let {
                            oldList.remove(it)
                        }
                        (ExtensionLoader.getExtension(
                            context,
                            pkgName
                        ) as? LoadResult.Success)?.let {
                            oldList.add(it.extension)
                        }
                        _installedExtensionsFlow.emit(ExtensionState.Extensions(oldList))
                    }
                }
            }
        }

        override fun onPackageUninstalled(context: Context, pkgName: String) {
            loadScope.launch {
                loadScope.launch {
                    if (loadingExtensionLabel.value != null) {
                        val job = loadScope.launch {
                            installedExtensionsFlow.collectLatest {
                                if (it is ExtensionState.Extensions) {
                                    val oldList = it.extensions.toMutableList()
                                    _installedExtensionsFlow.emit(ExtensionState.Loading)
                                    (ExtensionLoader.getExtension(
                                        context,
                                        pkgName
                                    ) as? LoadResult.Success)?.let {
                                        oldList.add(it.extension)
                                    }
                                    _installedExtensionsFlow.emit(ExtensionState.Extensions(oldList))
                                    cancel()
                                }
                            }

                        }
                        // 超时五秒钟
                        delay(5000)
                        job.cancel()
                    } else {
                        val lastState = _installedExtensionsFlow.value
                        if (lastState is ExtensionState.Extensions) {
                            val oldList = lastState.extensions.toMutableList()
                            _installedExtensionsFlow.emit(ExtensionState.Loading)
                            oldList.find { it.pkgName == pkgName }?.let {
                                oldList.remove(it)
                            }
                            _installedExtensionsFlow.emit(ExtensionState.Extensions(oldList))
                        }
                    }
                }
            }
        }

    }

}