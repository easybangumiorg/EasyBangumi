package com.heyanle.easybangumi4.ui.source_manage.extension

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.extension.Extension
import com.heyanle.easybangumi4.extension.ExtensionController
import com.heyanle.easybangumi4.extension.store.ExtensionStoreController
import com.heyanle.easybangumi4.extension.store.ExtensionStoreDispatcher
import com.heyanle.easybangumi4.extension.store.ExtensionStoreInfo
import com.heyanle.easybangumi4.extension.store.OfficialExtensionItem
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/21 23:26.
 * https://github.com/heyanLE
 */
class ExtensionViewModel : ViewModel() {


    private val extensionController: ExtensionController by Injekt.injectLazy()
    private val extensionStoreController: ExtensionStoreController by Injekt.injectLazy()
    private val extensionStoreDispatcher: ExtensionStoreDispatcher by Injekt.injectLazy()


    private val _stateFlow = MutableStateFlow<ExtensionState>(ExtensionState.Loading)
    val stateFlow = _stateFlow.asStateFlow()

    sealed class ExtensionState {
        data object Loading : ExtensionState()

        data class Info(
            val list: List<ExtensionItem>
        ) : ExtensionState()
    }


    sealed class ExtensionItem {

        // 拓展市场中的拓展
        data class StoreInfo(
            val info: ExtensionStoreInfo,
        ) : ExtensionItem()

        // 非拓展市场中的拓展
        // 1. 通过 App 方式加载的全部都属于这种类型
        // 2. 如果拓展市场加载失败，则全部拓展都直接走这种类型
        data class OtherInfo(
            val extension: Extension,
        )

    }

    init {
        viewModelScope.launch {

            combine(
                extensionStoreController.infoFlow,
                extensionController.state,
            ) { info, extensions ->
                if (extensions.isLoading) {
                    ExtensionState.Loading
                } else {
                    // 合并市场的和本地加载的
                    val storeList = (info as? ExtensionStoreController.ExtensionStoreState.Info)?.itemList?: emptyList<ExtensionStoreInfo>()

                }
            }

        }
    }

}