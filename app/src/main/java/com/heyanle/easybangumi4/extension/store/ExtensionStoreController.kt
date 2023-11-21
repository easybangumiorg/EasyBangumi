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
 * 1. 远端拓展列表 ExtensionStoreInfoRepository -> remoteStateFlow
 * 2. 下载中拓展列表 ExtensionStoreDispatcher.downloadInfoFlow
 * 3. 已下载拓展列表 ExtensionStoreDispatcher.installedExtension
 * 三个流合并成  infoFlow 表示最终展示到界面上的数据
 * Created by heyanlin on 2023/11/13.
 */
class ExtensionStoreController(
    private val extensionStoreDispatcher: ExtensionStoreDispatcher,
    private val extensionStoreInfoRepository: ExtensionStoreInfoRepository,
) {


    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var lastJob: Job? = null

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

        data class Error(
            val errorMsg: String,
            val throwable: Throwable? = null,
        ): ExtensionStoreState()

        data class Info(
            val itemList: List<ExtensionStoreInfo> = emptyList()
        ): ExtensionStoreState()
    }




    private val _remoteStateFlow = MutableStateFlow<RemoteExtensionStoreState>(RemoteExtensionStoreState.Loading)
    val remoteStateFlow = _remoteStateFlow.asStateFlow()


    private val _infoFlow = MutableStateFlow<ExtensionStoreState>(ExtensionStoreState.Loading)
    val infoFlow = _infoFlow.asStateFlow()


    init {
        scope.launch(Dispatchers.Main) {
            combine(
                remoteStateFlow,
                extensionStoreDispatcher.installedExtension,
                extensionStoreDispatcher.downloadInfoFlow,
            ) { remote, local, download ->
                if (remote is RemoteExtensionStoreState.Loading || local == null) {
                    ExtensionStoreState.Loading
                } else if (remote is RemoteExtensionStoreState.Error) {
                    ExtensionStoreState.Error(remote.errorMsg, remote.throwable)
                } else if(remote is RemoteExtensionStoreState.Info) {
                    val map = local ?: emptyMap()
                    val list = remote.itemList.map {
                        val downloadItem = download[it.pkg]
                        //val isDownloading = download.containsKey(it.pkg) && (download[it.pkg]?.job?.isActive == true)
                        val localItem = map[it.pkg]
                        var status = ExtensionStoreInfo.STATE_NO_DOWNLOAD
                        if (downloadItem != null) {

                            status = if (!downloadItem.isError && extensionStoreDispatcher.getJob(downloadItem.jobUUID)?.isActive == true) {
                                ExtensionStoreInfo.STATE_DOWNLOADING
                            } else {
                                ExtensionStoreInfo.STATE_ERROR
                            }
                        } else if (localItem != null) {
                            status = if (localItem.extensionStoreInfo.md5.uppercase() != it.md5.uppercase()) {
                                ExtensionStoreInfo.STATE_NEED_UPDATE
                            } else {
                                ExtensionStoreInfo.STATE_INSTALLED
                            }
                        }
                        ExtensionStoreInfo(
                            remote = it,
                            local = localItem,
                            state = status,
                            downloadItem = downloadItem,
                            downloadInfo = if(status ==  ExtensionStoreInfo.STATE_DOWNLOADING) extensionStoreDispatcher.getDownloadingInfo(it.pkg) else null,
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
    }

    init {
        refresh()
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


}