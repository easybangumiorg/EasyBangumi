package com.heyanle.easybangumi4.extension.store

import android.content.Context
import androidx.compose.ui.text.toUpperCase
import com.heyanle.easybangumi4.extension.ExtensionController
import com.heyanle.easybangumi4.utils.getFileMD5
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.jsonTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors

/**
 * 拓展市场 controller
 * 1. 远端拓展列表
 * 2. 远端拓展下载，下载到 cachePath 文件夹，一个拓展也就几 M, 这里不支持断点下载
 * 3. 拓展下载状态的管理
 * 4. 下载后通过 ExtensionController 加载
 * Created by heyanlin on 2023/11/13.
 */
class ExtensionStoreController(
    private val context: Context,
    private val extensionController: ExtensionController,
    private val extensionStoreInfoRepository: ExtensionStoreInfoRepository,
) {

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var lastJob: Job? = null

    private val rootFolder = File(context.getFilePath("extension"))
    private val extensionItemJson = File(rootFolder, "item.json")
    private val extensionItemJsonTemp = File(rootFolder, "item.json.bk")


    data class ExtensionStoreState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val errorMsg: String = "",
        val extensionStateItem: List<ExtensionStoreItem> = emptyList(),
    )

    private val _stateFlow = MutableStateFlow<ExtensionStoreState>(ExtensionStoreState(isLoading = true))
    val stateFlow = _stateFlow.asStateFlow()

    fun refresh(){
        _stateFlow.update {
            it.copy(
                isLoading = true
            )
        }
        lastJob?.cancel()
        lastJob = scope.launch() {
            yield()
            extensionStoreInfoRepository.getInfoList()
                .onError { err ->
                    yield()
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            isError = true,
                            errorMsg = err.errorMsg,
                        )
                    }
                }
                .onOK {
                    yield()
                    val list = it.infoList.map {
                        yield()
                        it.toExtensionStoreItem()
                    }
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            isError = false,
                            extensionStateItem = list
                        )
                    }
                }
        }
    }

    private fun ExtensionStoreInfoItem.toExtensionStoreItem(): ExtensionStoreItem{
        val installFile = File(extensionController.extensionFolder, )
        val downloadFile = File(rootFolder, "${pkg}${md5}.temp")
        TODO()
    }




}