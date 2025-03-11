package com.heyanle.easy_bangumi_cm.shared.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.homeComponent
import com.heyanle.lib.inject.core.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


/**
 * Created by HeYanLe on 2025/1/5 23:27.
 * https://github.com/heyanLE
 */

class HomeViewModel: ViewModel() {

    private val preferenceStore: PreferenceStore by Inject.injectLazy()
    private val selectionKeyPref: Preference<String> by preferenceStore.lazyString("home_selection_key", "")
    private val selectionKeyFlow = selectionKeyPref.stateIn(viewModelScope)

    private val sourceController: SourceController by Inject.injectLazy()

    data class State (
        // step.1
        val selectionKey: String = "",
        val sourceBundleState : DataState<SourceBundle> = DataState.Loading(),

        // step.2
        val homeContent: DataState<HomeContent> = DataState.None(),

        // step.3
        val selectionHomePage: HomePage? = null,


        // special
        val isSourcePanelShow: Boolean = false
    ) {
        val selectedInfo: SourceInfo.Loaded? by lazy {
            sourceBundleState.okOrNull()?.homeComponentInfo(selectionKey) ?:
            sourceBundleState.okOrNull()?.homeComponentInfoList()?.firstOrNull()
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
        // step.1 决定加载哪一个页面
        viewModelScope.launch {
            combine(
                sourceController.sourceBundleFlow,
                selectionKeyFlow,
            ) { bundleState, key ->
                bundleState to key
            }.collectLatest { sta ->
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
    }

    // source panel

    fun showSourcePanel(){
        _stateFlow.update { it.copy(isSourcePanelShow = true) }
    }

    fun hideSourcePanel(){
        _stateFlow.update { it.copy(isSourcePanelShow = false) }
    }

    fun toggleSourcePanel(){
        _stateFlow.update { it.copy(isSourcePanelShow = !it.isSourcePanelShow) }
    }

    fun changeSelectionKey(key: String){
        selectionKeyPref.set(key)
    }


}