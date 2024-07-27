package com.heyanle.easybangumi4.ui.source_manage.extension

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
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
import org.mozilla.javascript.JavaToJSONConverters
import java.io.File

/**
 * Created by HeYanLe on 2023/2/21 23:26.
 * https://github.com/heyanLE
 */
class ExtensionViewModel : ViewModel() {

    companion object {
        const val TAG = "ExtensionViewModel"
    }

    private val extensionController: ExtensionController by Inject.injectLazy()


    private val _stateFlow = MutableStateFlow(ExtensionState(true))
    val stateFlow = _stateFlow.asStateFlow()

    data class ExtensionState(
        val isLoading: Boolean = false,
        val list: List<ExtensionInfo> = emptyList(),
        val searchKey: String? = null, // 为 null 则 top bar 里没有输入法
        val showList: List<ExtensionInfo> = emptyList(), // 搜索后的数据
        val readyToDeleteFile: File? = null,
    )



    init {
        viewModelScope.launch {
            extensionController.state.collectLatest {extensions ->
                if (extensions.isLoading) {
                    _stateFlow.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                } else {
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            list = extensions.listExtensionInfo,
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
        item: ExtensionInfo,
        activity: Activity
    ) {

        if (item is ExtensionInfo.InstallError) {

            if (item.loadType == ExtensionInfo.TYPE_APK_FILE) {
                stringRes(com.heyanle.easy_i18n.R.string.click_to_install).moeSnackBar()
                IntentHelper.openAPK(item.sourcePath?:"", activity)
                return
            }


        } else if (item is ExtensionInfo.Installed) {
            if (item.loadType == ExtensionInfo.TYPE_APK_INSTALL) {
                IntentHelper.openAppDetailed(item.pkgName, APP)
            } else {
                stringRes(com.heyanle.easy_i18n.R.string.long_press_to_delete).moeSnackBar()
            }
        }

    }

    fun onItemLongPress(
        item: ExtensionInfo
    ) {
        if (item.isLoadFromFile) {
            _stateFlow.update {
                it.copy(
                    readyToDeleteFile = File(item.sourcePath)
                )
            }
        } else {
            IntentHelper.openAppDetailed(item.pkgName, APP)
        }
    }

    fun dismissDialog(){
        _stateFlow.update {
            it.copy(
                readyToDeleteFile = null
            )
        }
    }
}