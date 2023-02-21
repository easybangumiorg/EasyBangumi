package com.heyanle.extension_load

import android.content.Context
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
    sealed class ExtensionState {

        object None: ExtensionState()

        object Loading : ExtensionState()

        class ExtensionLoading(
            val extensions: List<Extension>,
            val loadingExtensionLabel: String,
        ) : ExtensionState()
        class Extensions(
            val extensions: List<Extension>
        ) : ExtensionState()
    }

    private val loadingExtension = AtomicBoolean(false)
    private val receiverInit = AtomicBoolean(false)

    private val _installedExtensionsFlow = MutableStateFlow<ExtensionState>(
        ExtensionState.None
    )
    val installedExtensionsFlow = _installedExtensionsFlow.asStateFlow()

    private val loadScope = MainScope()
    private var lastJob: Job? = null

    fun init(context: Context, loadPkgName: List<String>) {
        if(receiverInit.compareAndSet(false, true)){
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

    fun load(context: Context, extension: Extension.Available){
        loadScope.launch {
            val lastState = (_installedExtensionsFlow.value as? ExtensionState.Extensions) ?: return@launch
            if(loadingExtension.compareAndSet(false, true)){
                // cas 成功才加载
                _installedExtensionsFlow.emit(ExtensionState.ExtensionLoading(lastState.extensions, extension.label))
                val oldList = lastState.extensions.toMutableList()
                oldList.remove(extension)
                oldList.add(ExtensionLoader.loadExtension(context, extension))
                _installedExtensionsFlow.emit(ExtensionState.Extensions(oldList))
                loadingExtension.set(false)
            }else{
                // 有扩展正在加载，什么都不做
                return@launch
            }
        }
    }

    class ReceiverListener: ExtensionInstallReceiver.Listener {
        override fun onExtensionInstalled(context: Context, pkgName: String) {
            loadScope.launch {
                when(val lastState = _installedExtensionsFlow.value){
                    ExtensionState.None -> {}
                    ExtensionState.Loading -> {}
                    is ExtensionState.Extensions -> {
                        val oldList = lastState.extensions.toMutableList()
                        _installedExtensionsFlow.emit(ExtensionState.Loading)
                        (ExtensionLoader.getExtension(context, pkgName) as? LoadResult.Success)?.let {
                            oldList.add(it.extension)
                        }
                        _installedExtensionsFlow.emit(ExtensionState.Extensions(oldList))
                    }
                    is ExtensionState.ExtensionLoading -> {
                        // 如果当前有扩展在加载，避免冲突需要等待其加载完毕
                        val job = loadScope.launch {
                            installedExtensionsFlow.collectLatest {
                                if(it is ExtensionState.Extensions){
                                    val oldList = lastState.extensions.toMutableList()
                                    _installedExtensionsFlow.emit(ExtensionState.Loading)
                                    (ExtensionLoader.getExtension(context, pkgName) as? LoadResult.Success)?.let {
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
                    }
                }

            }
        }

        override fun onExtensionUpdated(context: Context, pkgName: String) {
            loadScope.launch {
                when(val lastState = _installedExtensionsFlow.value){
                    ExtensionState.None -> {}
                    ExtensionState.Loading -> {}
                    is ExtensionState.Extensions -> {
                        val oldList = lastState.extensions.toMutableList()
                        _installedExtensionsFlow.emit(ExtensionState.Loading)
                        oldList.find { it.pkgName == pkgName }?.let {
                            oldList.remove(it)
                        }
                        (ExtensionLoader.getExtension(context, pkgName) as? LoadResult.Success)?.let {
                            oldList.add(it.extension)
                        }
                        _installedExtensionsFlow.emit(ExtensionState.Extensions(oldList))
                    }
                    is ExtensionState.ExtensionLoading -> {
                        // 如果当前有扩展在加载，避免冲突需要等待其加载完毕
                        val job = loadScope.launch {
                            installedExtensionsFlow.collectLatest {
                                if(it is ExtensionState.Extensions){
                                    val oldList = lastState.extensions.toMutableList()
                                    _installedExtensionsFlow.emit(ExtensionState.Loading)
                                    (ExtensionLoader.getExtension(context, pkgName) as? LoadResult.Success)?.let {
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
                    }
                }
            }
        }

        override fun onPackageUninstalled(context: Context, pkgName: String) {
            loadScope.launch {
                loadScope.launch {
                    when(val lastState = _installedExtensionsFlow.value){
                        ExtensionState.None -> {}
                        ExtensionState.Loading -> {}
                        is ExtensionState.Extensions -> {
                            val oldList = lastState.extensions.toMutableList()
                            _installedExtensionsFlow.emit(ExtensionState.Loading)
                            oldList.find { it.pkgName == pkgName }?.let {
                                oldList.remove(it)
                            }
                            _installedExtensionsFlow.emit(ExtensionState.Extensions(oldList))
                        }
                        is ExtensionState.ExtensionLoading -> {
                            // 如果当前有扩展在加载，避免冲突需要等待其加载完毕
                            val job = loadScope.launch {
                                installedExtensionsFlow.collectLatest {
                                    if(it is ExtensionState.Extensions){
                                        val oldList = lastState.extensions.toMutableList()
                                        _installedExtensionsFlow.emit(ExtensionState.Loading)
                                        (ExtensionLoader.getExtension(context, pkgName) as? LoadResult.Success)?.let {
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
                        }
                    }
                }
            }
        }

    }

}