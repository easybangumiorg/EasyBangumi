package com.heyanle.easybangumi4.ui.source_manage.extension

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.remote.ExtensionRemoteLocalInfo
import com.heyanle.easybangumi4.plugin.extension.remote.ExtensionRepoController
import com.heyanle.easybangumi4.plugin.extension.remote.RemoteInfo
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.IntentHelper
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.collections.filter
import kotlin.getValue

/**
 * Created by heyanle on 2025/8/16
 * https://github.com/heyanLE
 */
class ExtensionV2ViewModel : ViewModel() {

    companion object {
        const val TAG = "ExtensionV2ViewModel"
    }

    private val extensionRemoteController: ExtensionRepoController by Inject.injectLazy()


    private val _stateFlow = MutableStateFlow(ExtensionState(true))
    val stateFlow = _stateFlow.asStateFlow()

    data class ExtensionState(
        val isLoading: Boolean = false,
        val isRemoteLoading: Boolean = false,
        val list: List<ExtensionRemoteLocalInfo> = emptyList(),
        val searchKey: String? = null, // 为 null 则 top bar 里没有输入法
        val showList: List<ExtensionRemoteLocalInfo> = emptyList(), // 搜索后的数据
        val dialog: Dialog? = null,
    )


    sealed class Dialog {
        data class DeleteFile(
            val info:  ExtensionInfo.Installed
        ) : Dialog()

        data class ReadyToDownload(
            val remoteInfo: RemoteInfo
        ) : Dialog()

        data class Downloading(
            val remoteInfo: RemoteInfo,
        ) : Dialog()
    }


    init {
        viewModelScope.launch {
            extensionRemoteController.state.collectLatest { info ->
                info.logi(TAG)
                if (info.loading) {
                    _stateFlow.update {
                        it.copy(
                            isLoading = true,
                            isRemoteLoading = info.remoteLoading,
                        )
                    }
                } else {
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            list = info.remoteLocalInfo.values.toList(),
                            isRemoteLoading = info.remoteLoading,
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            combine(
                _stateFlow.map { it.searchKey }.distinctUntilChanged().stateIn(viewModelScope),
                _stateFlow.map { if (it.isLoading) null else it.list }.distinctUntilChanged()
                    .stateIn(viewModelScope),
            ) { search, list ->
                "feedback Flow".logi(TAG)
                if (list == null) {
                    return@combine
                } else {
                    val searchList = if (search == null) {
                        list
                    } else {
                        list.filter { it.match(search) }
                    }

                    _stateFlow.update {
                        it.copy(
                            showList = searchList
                        )
                    }
                }

            }.collect()
        }
    }

    fun onSearchChange(searchKey: String?) {
        _stateFlow.update {
            it.copy(
                searchKey = searchKey
            )
        }
    }


    fun refresh() {

    }

    fun onItemClick(
        remoteLocalInfo: ExtensionRemoteLocalInfo,
        activity: Activity
    ) {
        if (remoteLocalInfo.onlyRemote && remoteLocalInfo.remoteInfo != null) {
            _stateFlow.update {
                it.copy(
                    dialog = Dialog.ReadyToDownload(remoteLocalInfo.remoteInfo)
                )
            }
        } else if (remoteLocalInfo.hasUpdate &&
            remoteLocalInfo.remoteInfo != null) {
            _stateFlow.update {
                it.copy(
                    dialog = Dialog.ReadyToDownload(remoteLocalInfo.remoteInfo)
                )
            }
        } else if (remoteLocalInfo.localInfo != null) {
            if (remoteLocalInfo.localInfo is ExtensionInfo.Installed) {
                stringRes(com.heyanle.easy_i18n.R.string.long_press_to_delete).moeSnackBar()
            }

        }
    }

    fun onDownload(
        remoteInfo: RemoteInfo
    ) {
        _stateFlow.update {
            it.copy(
                dialog = Dialog.Downloading(remoteInfo)
            )
        }
        viewModelScope.launch {
            extensionRemoteController.appendOrUpdate(remoteInfo)
                .onOK {
                    stringRes(com.heyanle.easy_i18n.R.string.succeed).moeSnackBar()
                }.onError {
                    it.errorMsg.logi(TAG)
                    it.throwable?.printStackTrace()
                    it.errorMsg.moeSnackBar()
                }
            dismissDialog()
        }

    }


    fun onItemLongPress(
        item: ExtensionRemoteLocalInfo
    ) {
        if (item.localInfo is ExtensionInfo.Installed) {
            _stateFlow.update {
                it.copy(
                    dialog = Dialog.DeleteFile(item.localInfo)
                )
            }
        }
    }

    fun onDelete(localInfo: ExtensionInfo.Installed) {
        dismissDialog()
        viewModelScope.launch {
            extensionRemoteController.delete(localInfo.key)
                .onOK {
                    stringRes(com.heyanle.easy_i18n.R.string.succeed).moeSnackBar()
                }.onError {
                    it.errorMsg.logi(TAG)
                    it.throwable?.printStackTrace()
                    it.errorMsg.moeSnackBar()
                }
        }

    }

    fun dismissDialog() {
        _stateFlow.update {
            it.copy(
                dialog = null
            )
        }
    }
}