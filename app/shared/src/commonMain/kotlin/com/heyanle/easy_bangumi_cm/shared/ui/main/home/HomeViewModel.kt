package com.heyanle.easy_bangumi_cm.shared.ui.main.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easy_bangumi_cm.base.service.system.logger
import com.heyanle.easy_bangumi_cm.base.utils.DataState
import com.heyanle.easy_bangumi_cm.base.utils.preference.Preference
import com.heyanle.easy_bangumi_cm.base.utils.preference.PreferenceStore
import com.heyanle.easy_bangumi_cm.base.utils.preference.lazyString
import com.heyanle.easy_bangumi_cm.base.utils.resources.ResourceOr
import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceInfo
import com.heyanle.easy_bangumi_cm.common.plugin.core.source.SourceBundle
import com.heyanle.easy_bangumi_cm.common.plugin.core.source.SourceController
import com.heyanle.easy_bangumi_cm.plugin.api.base.toDataState
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeContent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.homeComponent
import com.heyanle.easy_bangumi_cm.plugin.api.source.SourceManifest
import com.heyanle.lib.inject.core.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


/**
 * Created by HeYanLe on 2025/1/5 23:27.
 * https://github.com/heyanLE
 */

class HomeViewModel : ViewModel() {

    companion object {
        const val TAG = "HomeViewModel"
    }

    private val preferenceStore: PreferenceStore by Inject.injectLazy()
    private val selectionKeyPref: Preference<String> by preferenceStore.lazyString("home_selection_key", "")
    private val selectionKeyFlow = selectionKeyPref.stateIn(viewModelScope)

    private val sourceController: SourceController by Inject.injectLazy()

    data class SourceUIState (
        val sourceHomeList: List<SourceManifest>,  // key to label
        val selectionKey: String,
        val topAppLabel: ResourceOr,
        val isSourcePanelShow: Boolean,
    )

    data class HomeContentUIState (
        val homeContent: HomeContent,
    )

    data class UIState(
        // 番源状态 - 番源列表 当前选中番源 番源面板是否展开
        val sourceUIState: DataState<SourceUIState> = DataState.loading(),
        // 内容状态 - 选中番源的 HomeContent 加载状态
        val homeContentUIState: DataState<HomeContentUIState> = DataState.loading(),
    )
    val uiState = mutableStateOf<UIState>(UIState())

    data class State(
        // step.1
        val selectionKey: String = "",
        val sourceBundleState: DataState<SourceBundle> = DataState.Loading(),

        // step.2
        val homeContent: DataState<HomeContent> = DataState.None(),

        // special
        val isSourcePanelShow: Boolean = false
    ) {
        val realSelectKey: String? by lazy {
            if (sourceBundleState.okOrNull()?.homeComponentInfo(selectionKey) != null) selectionKey else null
        }
        val selectedInfo: SourceInfo.Loaded? by lazy {
            sourceBundleState.okOrNull()?.homeComponentInfo(selectionKey) ?: sourceBundleState.okOrNull()
                ?.homeComponentInfoList()?.firstOrNull()
        }
        val selectedHomeComponent: HomeComponent? by lazy {
            selectedInfo?.componentBundle?.homeComponent()
        }
        val topAppLabel: ResourceOr? by lazy {
            selectedInfo?.manifest?.label
        }

    }

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow = _stateFlow.asStateFlow()

    init {
        // step.1 番源加载 & 当前选中 Key 读取
        viewModelScope.launch {
            combine(
                sourceController.sourceBundleFlow,
                selectionKeyFlow,
            ) { bundleState, key ->
                bundleState to key
            }.collectLatest { sta ->
                logger.i(TAG, "step 1 ${sta.first}")
                _stateFlow.update {
                    it.copy(
                        sourceBundleState = sta.first,
                        selectionKey = sta.second,
                    )
                }
            }
        }

        // step.2 加载页面数据
        viewModelScope.launch {
            stateFlow.map { it.selectedHomeComponent }.filterNotNull().distinctUntilChanged()
                .collectLatest {
                    // loading
                    _stateFlow.update { it.copy(homeContent = DataState.Loading()) }
                    val res = it.home()
                    _stateFlow.update { it.copy(homeContent = res.toDataState()) }
                }
        }

        // state -> uiState
        viewModelScope.launch {
            stateFlow.collectLatest {
                val sourceUIState = when (it.sourceBundleState) {
                    is DataState.Error -> DataState.error(
                        it.sourceBundleState.errorMsg,
                        it.sourceBundleState.throwable
                    )
                    is DataState.None -> DataState.loading()
                    is DataState.Loading -> DataState.loading()
                    is DataState.Ok -> {
                        val sourceHomeList = arrayListOf<SourceManifest>()
                        sourceHomeList.addAll(
                            it.sourceBundleState.data.homeComponentInfoList()
                                .map { it.manifest })
                        if (sourceHomeList.isEmpty())
                            DataState.empty()
                        else
                            DataState.ok(SourceUIState(
                                sourceHomeList,
                                it.realSelectKey ?: "",
                                it.topAppLabel ?: "",
                                it.isSourcePanelShow
                            ))
                    }
                }
                val homeContentUIState = when (it.homeContent) {
                    is DataState.Error -> DataState.error(it.homeContent.errorMsg, it.homeContent.throwable)
                    is DataState.None -> DataState.loading()
                    is DataState.Loading -> DataState.loading()
                    is DataState.Ok -> DataState.ok(HomeContentUIState(it.homeContent.data))
                }
                uiState.value = UIState(sourceUIState, homeContentUIState)
            }
        }
    }

    // 刷新番源引擎
    fun refreshSource() {
        sourceController.refresh()
    }

    // 刷新主页内容
    fun refreshHomeContent() {
        sourceController.sourceBundleFlow
        viewModelScope.launch {
            val current = stateFlow.value.selectedHomeComponent ?: return@launch
            // loading
            _stateFlow.update { it.copy(homeContent = DataState.Loading()) }
            val res = current.home()
            _stateFlow.update { it.copy(homeContent = res.toDataState()) }
        }
    }

    // source panel

    fun showSourcePanel() {
        _stateFlow.update { it.copy(isSourcePanelShow = true) }
    }

    fun hideSourcePanel() {
        _stateFlow.update { it.copy(isSourcePanelShow = false) }
    }

    fun toggleSourcePanel() {
        _stateFlow.update { it.copy(isSourcePanelShow = !it.isSourcePanelShow) }
    }

    fun changeSelectionKey(key: String) {
        logger.i(TAG, "changeSelectionKey: $key")
        selectionKeyPref.set(key)
    }

}