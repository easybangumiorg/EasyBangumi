package com.heyanle.easybangumi4.ui.extension_store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.extension.store.ExtensionStoreController
import com.heyanle.easybangumi4.extension.store.ExtensionStoreDispatcher
import com.heyanle.easybangumi4.extension.store.ExtensionStoreInfo
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2023/11/20.
 */
class ExtensionStoreViewModel : ViewModel() {

    private val extensionStoreController: ExtensionStoreController by Injekt.injectLazy()
    private val extensionStoreDispatcher: ExtensionStoreDispatcher by Injekt.injectLazy()

    // 状态
    val infoFlow = extensionStoreController.infoFlow.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        extensionStoreController.infoFlow.value
    )

    val currentShow = MutableStateFlow<List<ExtensionStoreInfo>>(emptyList())

    val currentTabIndex = MutableStateFlow(0)

    val allInfo = MutableStateFlow<List<ExtensionStoreInfo>>(emptyList())


    // 搜索 Key
    val searchKey = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            combine(
                searchKey,
                infoFlow.filterIsInstance<ExtensionStoreController.ExtensionStoreState.Info>()
                    .map { info ->
                        info.itemList
                    }.distinctUntilChanged()
            ) { key, list ->
                if (!key.isNullOrEmpty()) {
                    list.filter { it.match(key) }
                } else {
                    list
                }
            }.collectLatest { list ->
                allInfo.update {
                    list
                }
            }
        }
        viewModelScope.launch {
            combine(
                allInfo,
                currentTabIndex
            ){ a: List<ExtensionStoreInfo>, b: Int ->
                when(b){
                    0 -> a
                    1 -> a.filter {
                        it.state == ExtensionStoreInfo.STATE_NEED_UPDATE
                    }
                    2 -> a.filter {
                        it.state == ExtensionStoreInfo.STATE_INSTALLED || it.state == ExtensionStoreInfo.STATE_NEED_UPDATE
                    }
                    3 -> a.filter {
                        it.state == ExtensionStoreInfo.STATE_ERROR || it.state == ExtensionStoreInfo.STATE_DOWNLOADING
                    }
                    else -> {
                        emptyList<ExtensionStoreInfo>()
                    }
                }
            }.collectLatest { s ->
                currentShow.update {
                    s
                }
            }
        }
    }

    fun onTabChange(tab: Int){
        currentTabIndex.update {
            tab
        }
    }

    fun onClick(info: ExtensionStoreInfo) {
        extensionStoreDispatcher.toggle(info.remote)
    }

    fun onSearch(key: String?){
        searchKey.update {
            key
        }
    }

    fun refresh(){
        extensionStoreController.refresh()
    }

}