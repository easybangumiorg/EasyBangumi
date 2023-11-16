package com.heyanle.easybangumi4.extension.store

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import com.heyanle.easybangumi4.extension.ExtensionController
import com.heyanle.easybangumi4.utils.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.File
import java.util.concurrent.Executors

/**
 * 拓展市场 controller
 * 1. 远端拓展列表
 * 2. 远端拓展下载，下载到 cachePath 文件夹，一个拓展一般 600k，顶天也就 几m , 这里不支持断点下载
 * 3. 下载后复制到 extension 文件夹后在写入 json 文件，用于版本更新判断
 * 4. 在 cachePath 中只有当前启动正在下载的文件才保留，其他文件随时都会被删除
 *
 * 已下载源的 json 文件 official.json                     ↘
 * 源市场数据 RemoteExtensionStoreState                  ➡ ExtensionStoreState 市场页展示数据
 * 下载器的下载任务 Map<String, ExtensionDownloadInfo     ↗
 * Created by heyanlin on 2023/11/13.
 */
class ExtensionStoreController(
    private val context: Context,
    private val extensionController: ExtensionController,
    private val extensionStoreInfoRepository: ExtensionStoreInfoRepository,
) {


    @OptIn(ExperimentalCoroutinesApi::class)
    private val singleDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var lastJob: Job? = null

    private val rootFolder = File(extensionController.extensionFolder)
    private val extensionItemJson = File(rootFolder, "official.json")
    private val extensionItemJsonTemp = File(rootFolder, "official.json.bk")

    sealed class RemoteExtensionStoreState{
        data object Loading: RemoteExtensionStoreState()

        class Error(
            val errorMsg: String,
            val throwable: Throwable? = null,
        ): RemoteExtensionStoreState()

        class Info(
            val itemList: List<ExtensionStoreRemoteInfoItem> = emptyList(),
        ): RemoteExtensionStoreState()
    }

    sealed class ExtensionStoreState {
        data object Loading: ExtensionStoreState()

        class Error(
            val errorMsg: String,
            val throwable: Throwable? = null,
        ): ExtensionStoreState()

        class Info(
            val itemList: List<ExtensionStoreInfo> = emptyList()
        ): ExtensionStoreState()
    }


    // 番剧下载 Info
    // 只要 job 是 active 状态就代表正在下载，不支持断点续传
    class ExtensionDownloadInfo(
        val job: Job?,
        val downloadPath: String,
        val extensionPath: String,
        val fileName: String,
        val isError: Boolean = false,
        val errorMsg: String = "",
        val throwable: Throwable? = null,
        val remoteInfo: ExtensionStoreRemoteInfo,
        val status: MutableState<String> = mutableStateOf(""),
        val subStatus: MutableState<String> = mutableStateOf(""),
        val process: MutableState<Float> = mutableFloatStateOf(-1f)
    )


    private val _remoteStateFlow = MutableStateFlow<RemoteExtensionStoreState>(RemoteExtensionStoreState.Loading)
    val remoteStateFlow = _remoteStateFlow.asStateFlow()

    private val _installedExtension = MutableStateFlow<List<OfficialExtensionItem>?>(null)
    val installedExtension = _installedExtension.asStateFlow()

    private var lastSaveJob: Job? = null

    private val _infoFlow = MutableStateFlow<ExtensionStoreState>(ExtensionStoreState.Loading)
    val infoFlow = _infoFlow.asStateFlow()


    // 不支持断点下载，该数据不用考虑持久化
    private val _downloadInfoFlow = MutableStateFlow<Map<String, ExtensionDownloadInfo>>(emptyMap())
    val downloadInfoFlow = _downloadInfoFlow.asStateFlow()


    init {
        scope.launch(Dispatchers.Main) {
            combine(
                remoteStateFlow,
                installedExtension,
                _downloadInfoFlow,
            ) { remote, local, download ->
                if (remote is RemoteExtensionStoreState.Loading || local == null) {
                    ExtensionStoreState.Loading
                } else if (remote is RemoteExtensionStoreState.Error) {
                    ExtensionStoreState.Error(remote.errorMsg, remote.throwable)
                } else if(remote is RemoteExtensionStoreState.Info) {
                    val map = hashMapOf<String, OfficialExtensionItem>()
                    local.forEach {
                        map[it.extensionStoreInfo.pkg] = it
                    }
                    val list = remote.itemList.map {
                        val downloadItem = download[it.pkg]
                        //val isDownloading = download.containsKey(it.pkg) && (download[it.pkg]?.job?.isActive == true)
                        val localItem = map[it.pkg]
                        var status = ExtensionStoreInfo.STATE_NO_DOWNLOAD
                        if (downloadItem != null) {
                            if (!downloadItem.isError && downloadItem.job?.isActive == true) {
                                status = ExtensionStoreInfo.STATE_DOWNLOADING
                            } else {
                                status = ExtensionStoreInfo.STATE_ERROR
                            }
                        } else if (localItem != null) {
                            if (localItem.extensionStoreInfo.md5.uppercase() != it.md5.uppercase()) {
                                status = ExtensionStoreInfo.STATE_NEED_UPDATE
                            } else {
                                status = ExtensionStoreInfo.STATE_INSTALLED
                            }
                        }
                        ExtensionStoreInfo(
                            remote = it,
                            local = localItem,
                            state = status,
                            errorMsg = downloadItem?.errorMsg?:"",
                            throwable = downloadItem?.throwable
                        )
                    }
                    ExtensionStoreState.Info(list)
                }else {
                    throw IllegalAccessException("never run here")
                }
            }.collectLatest {  sta ->
                _infoFlow.update {
                    sta
                }
            }
        }

        scope.launch {
            _installedExtension.collectLatest {
                if(it != null){
                    saveJson(it)
                }
            }
        }
    }

    fun refresh(){
        _remoteStateFlow.update {
            RemoteExtensionStoreState.Loading
        }
        lastJob?.cancel()
        lastJob = scope.launch() {
            yield()
            extensionStoreInfoRepository.getInfoList()
                .onError { err ->
                    yield()
                    _remoteStateFlow.update {
                        RemoteExtensionStoreState.Error(
                            err.errorMsg,
                            err.throwable
                        )
                    }
                }
                .onOK { info ->
                    yield()
                    _remoteStateFlow.update {
                        RemoteExtensionStoreState.Info(
                            info.extensionsV1
                        )
                    }
                }
        }
    }

    private fun saveJson(list: List<OfficialExtensionItem>) {
        lastSaveJob?.cancel()
        lastSaveJob = scope.launch(singleDispatcher) {
            extensionItemJsonTemp.delete()
            extensionItemJsonTemp.createNewFile()
            yield()
            extensionItemJsonTemp.writeText(list.toJson())
            yield()
            extensionItemJson.delete()
            extensionItemJsonTemp.renameTo(extensionItemJson)
            yield()
        }
    }



}