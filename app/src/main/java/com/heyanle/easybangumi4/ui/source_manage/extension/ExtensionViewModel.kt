package com.heyanle.easybangumi4.ui.source_manage.extension

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.extension.ExtensionInfo
import com.heyanle.easybangumi4.extension.ExtensionController
import com.heyanle.easybangumi4.extension.store.ExtensionStoreController
import com.heyanle.easybangumi4.extension.store.ExtensionStoreDispatcher
import com.heyanle.easybangumi4.extension.store.ExtensionStoreInfo
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.IntentHelper
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by HeYanLe on 2023/2/21 23:26.
 * https://github.com/heyanLE
 */
class ExtensionViewModel : ViewModel() {

    companion object {
        const val TAG = "ExtensionViewModel"
    }

    private val extensionController: ExtensionController by Injekt.injectLazy()
    private val extensionStoreController: ExtensionStoreController by Injekt.injectLazy()
    private val extensionStoreDispatcher: ExtensionStoreDispatcher by Injekt.injectLazy()

    private val _stateFlow = MutableStateFlow(ExtensionState(true))
    val stateFlow = _stateFlow.asStateFlow()

    data class ExtensionState(
        val isLoading: Boolean = false,
        val list: List<ExtensionItem> = emptyList(),
        val searchKey: String? = null, // 为 null 则 top bar 里没有输入法
        val showList: List<ExtensionItem> = emptyList(), // 搜索后的数据
        val currentTab: Int = 0,
        val readyToDeleteFile: File? = null,
    )

    sealed class ExtensionItem {

        // 拓展市场中的拓展
        data class StoreInfo(
            val info: ExtensionStoreInfo,
        ) : ExtensionItem() {
            override fun match(searchKey: String): Boolean {
                return info.match(searchKey)
            }
        }

        // 已经安装的拓展市场中的拓展
        data class StoreExtensionInfo(
            val info: ExtensionStoreInfo,
            val extensionInfo: com.heyanle.easybangumi4.extension.ExtensionInfo,
        ) : ExtensionItem() {
            override fun match(searchKey: String): Boolean {
                return info.match(searchKey) || extensionInfo.match(searchKey)
            }
        }

        // 非拓展市场中的拓展
        // 1. 通过 App 方式加载的全部都属于这种类型
        // 2. 如果拓展市场加载失败，则全部拓展都直接走这种类型
        // 3. 直接复制文件到拓展目录的拓展也属于这种类型
        data class ExtensionInfo(
            val extensionInfo: com.heyanle.easybangumi4.extension.ExtensionInfo,
        ) : ExtensionItem() {
            override fun match(searchKey: String): Boolean {
                return extensionInfo.match(searchKey)
            }
        }

        abstract fun match(searchKey: String): Boolean
    }

    init {
        viewModelScope.launch {
            combine(
                extensionStoreController.infoFlow,
                extensionController.state,
            ) { info, extensions ->
                "main Flow".logi(TAG)
                if (extensions.isLoading) {
                    _stateFlow.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                } else {
                    // 合并市场的和本地加载的
                    val res = arrayListOf<ExtensionItem>()
                    val storeList =
                        (info as? ExtensionStoreController.ExtensionStoreState.Info)?.itemList
                            ?: emptyList<ExtensionStoreInfo>()
                    val storeMap = hashMapOf<String, ExtensionStoreInfo>()
                    storeList.forEach { extensionStoreInfo ->
                        if (extensionStoreInfo.local != null) {
                            extensionStoreInfo.local.let {
                                storeMap[it.realFilePath] = extensionStoreInfo
                            }
                        } else {
                            res.add(ExtensionItem.StoreInfo(extensionStoreInfo))
                        }

                    }
                    (extensions.appExtensions.values + extensions.fileExtensionInfo.values).forEach {
                        val storeInfo = storeMap[it.publicPath]
                        if (storeInfo == null) {
                            res.add(ExtensionItem.ExtensionInfo(it))
                        } else {
                            res.add(ExtensionItem.StoreExtensionInfo(storeInfo, it))
                            storeMap.remove(it.publicPath)
                        }
                    }
                    storeMap.values.forEach {
                        res.add(ExtensionItem.StoreInfo(it))
                    }
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            list = res.sortedBy {
                                when (it) {
                                    is ExtensionItem.ExtensionInfo -> 0
                                    is ExtensionItem.StoreExtensionInfo -> 1
                                    else -> 3
                                }
                            },
                        )
                    }
                }
            }.collect()
        }

        viewModelScope.launch {
            combine(
                _stateFlow.map { it.searchKey }.distinctUntilChanged().stateIn(viewModelScope),
                _stateFlow.map { it.currentTab }.distinctUntilChanged().stateIn(viewModelScope),
                _stateFlow.map { if (it.isLoading) null else it.list }.distinctUntilChanged()
                    .stateIn(viewModelScope),
            ) { search, tab, list ->
                "feedback Flow".logi(TAG)
                if (list == null) {
                    return@combine
                } else {
                    val searchList = if (search == null) {
                        list
                    } else {
                        list.filter { it.match(search) }
                    }
                    val showList = when (tab) {
                        0 -> searchList
                        1 -> searchList.filter {
                            it is ExtensionItem.StoreExtensionInfo && it.info.state == ExtensionStoreInfo.STATE_NEED_UPDATE
                        }

                        2 -> searchList.filter {
                            (it is ExtensionItem.StoreExtensionInfo &&
                                    (it.info.state == ExtensionStoreInfo.STATE_INSTALLED || it.info.state == ExtensionStoreInfo.STATE_NEED_UPDATE))
                                    || (it is ExtensionItem.ExtensionInfo)
                        }

                        3 -> searchList.filter {
                            it is ExtensionItem.StoreInfo && (it.info.state == ExtensionStoreInfo.STATE_ERROR || it.info.state == ExtensionStoreInfo.STATE_DOWNLOADING)
                        }

                        else -> emptyList()
                    }

                    _stateFlow.update {
                        it.copy(
                            showList = showList
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
        extensionStoreController.refresh()
    }

    fun onTabClick(index: Int) {
        _stateFlow.update {
            it.copy(
                currentTab = index
            )
        }
    }

    fun onItemClick(
        item: ExtensionItem,
        activity: Activity
    ) {
        when (item) {
            is ExtensionItem.StoreExtensionInfo -> {
                val ext = item.extensionInfo
                val info = item.info

                if (info.state == ExtensionStoreInfo.STATE_INSTALLED) {
                    if (ext.loadType == ExtensionInfo.TYPE_APP) {
                        IntentHelper.openAppDetailed(ext.pkgName, APP)
                    } else {
                        stringRes(com.heyanle.easy_i18n.R.string.long_press_to_delete).moeSnackBar()
                    }
                } else {
                    extensionStoreDispatcher.toggle(item.info.remote)
                }
            }

            is ExtensionItem.ExtensionInfo -> {
                val ext = item.extensionInfo
                if (ext.loadType == ExtensionInfo.TYPE_APP) {
                    IntentHelper.openAppDetailed(ext.pkgName, APP)
                } else {
                    stringRes(com.heyanle.easy_i18n.R.string.long_press_to_delete).moeSnackBar()
                }
            }

            is ExtensionItem.StoreInfo -> {
                if (item.info.state == ExtensionStoreInfo.STATE_INSTALLED) {
                    stringRes(com.heyanle.easy_i18n.R.string.click_to_install).moeSnackBar()
                    IntentHelper.openAPK(item.info.local?.realFilePath?:"", activity)
                } else {
                    extensionStoreDispatcher.toggle(item.info.remote)
                }

            }
        }
    }

    fun onItemLongPress(
        item: ExtensionItem
    ) {
        when (item) {
            is ExtensionItem.ExtensionInfo -> {
                val ext = item.extensionInfo
                if (ext.loadType == ExtensionInfo.TYPE_APP) {
                    IntentHelper.openAppDetailed(ext.pkgName, APP)
                } else {
                    item.extensionInfo.sourcePath?.let { path ->
                        _stateFlow.update {
                            it.copy(
                                readyToDeleteFile = File(path)
                            )
                        }
                    }
                }
            }

            is ExtensionItem.StoreExtensionInfo -> {
                val ext = item.extensionInfo
                val info = item.info

                if (info.state == ExtensionStoreInfo.STATE_INSTALLED) {
                    if (ext.loadType == ExtensionInfo.TYPE_APP) {
                        IntentHelper.openAppDetailed(ext.pkgName, APP)
                    } else {
                        item.extensionInfo.sourcePath?.let {
                            File(it).delete()
                        }
                        item.info.local?.let {
                            extensionStoreDispatcher.removeInstalled(item.info.local)
                        }
                    }
                } else {
                    extensionStoreDispatcher.remove(item.info.remote.pkg)
                }
            }

            is ExtensionItem.StoreInfo -> {

            }
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