package org.easybangumi.next.shared.ui.main.home

import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.store.preference.PreferenceStore
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.api.component.page.CartoonPage
import org.easybangumi.next.shared.plugin.api.component.page.PageComponent
import org.easybangumi.next.shared.plugin.api.component.page.PageTab
import org.easybangumi.next.shared.plugin.api.toDataState
import org.easybangumi.next.shared.plugin.core.component.ComponentBundle
import org.easybangumi.next.shared.plugin.core.component.ComponentBusiness
import org.easybangumi.next.shared.plugin.core.info.SourceInfo
import org.easybangumi.next.shared.plugin.core.source.SourceBundle
import org.easybangumi.next.shared.plugin.core.source.SourceController
import org.easybangumi.next.shared.resources.Res
import org.koin.core.component.inject

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

class HomeViewModel(
    private val sourceBundle: SourceBundle
): StateViewModel<HomeViewModel.State>(State(sourceBundle)) {

    private val preferenceStore: PreferenceStore by inject()
    private val sourceController: SourceController by inject()

    private val selectionSourceKeyPref = preferenceStore.getString("selection_source_key", "")


    sealed class TabState {
        data class Discover(
            val discoverBusiness: ComponentBusiness<DiscoverComponent>,
        ): TabState() {

            override val label: ResourceOr by lazy {
                Res.strings.discover
            }

        }

        data class Page(
            val pageBusiness: ComponentBusiness<PageComponent>,
            val cartoonPage: CartoonPage,
        ): TabState() {

            override val label: ResourceOr by lazy {
                cartoonPage.name
            }

        }

        abstract val label: ResourceOr
    }

    data class PageState(
        val discoverTab: TabState.Discover? = null,
        val isSelectedDiscover: Boolean = false,

        val pageList: DataState<List<TabState.Page>> = DataState.none(),
        val selection: Int = -1,
    ) {

        val showTabList: List<TabState> by lazy {
            val list = mutableListOf<TabState>()
            if (discoverTab != null) {
                list.add(discoverTab)
            }
            pageList.onOK {
                list.addAll(it)
            }
            return@lazy list
        }

        val selectedTab: TabState? by lazy {
            if (isSelectedDiscover && discoverTab != null) {
                return@lazy discoverTab
            }
            return@lazy showTabList.getOrNull(selection)
        }

    }

    sealed class Popup {
        data class SourceChange(
            val sourceBundle: SourceBundle,
        ): Popup()
    }

    data class State(
        val sourceBundle: SourceBundle,
        val selectionSourceKey: String = "",
        val pageState: PageState = PageState(),
        val popup: Popup? = null,
    ) {
        val realSelectionKey: String? by lazy {
            if (sourceBundle.contains(selectionSourceKey)) selectionSourceKey else sourceBundle.keys().firstOrNull()
        }

        val componentBundle: ComponentBundle? by lazy {
            realSelectionKey?.let { sourceBundle.componentBundle(it) }
        }

        val sourceInfo: SourceInfo.Loaded? by lazy {
            realSelectionKey?.let { sourceBundle.sourceInfo(it) }
        }

    }


    init {
        // 1. source key pref
        viewModelScope.launch {
            selectionSourceKeyPref.flow().collectLatest { key ->
                update {
                    it.copy(
                        selectionSourceKey = key
                    )
                }
            }
        }


        // 2. load page
        viewModelScope.launch {
            state.map { it.componentBundle }.distinctUntilChanged().collectLatest { bundle ->
                if (bundle == null) {
                    update {
                        it.copy(pageState = PageState())
                    }
                    return@collectLatest
                }


                var tempPage = PageState()
                fun updatePageState() {
                    update {
                        it.copy(pageState = tempPage.copy())
                    }
                }

                val discoverComponent = bundle.getBusiness(DiscoverComponent::class)
                val discoverTab = if (discoverComponent != null) {
                    TabState.Discover(discoverComponent)
                } else {
                    null
                }


                if (discoverTab != null) {
                    // 提前展示发现页
                    tempPage = tempPage.copy(
                        discoverTab = discoverTab,
                        isSelectedDiscover = true,
                    )
                    updatePageState()
                }


                val pageBusiness = bundle.getBusiness(PageComponent::class)
                if (pageBusiness == null) {
                    updatePageState()
                    return@collectLatest
                }
                tempPage = tempPage.copy(
                    pageList = DataState.loading()
                )
                updatePageState()


                val pageListState = pageBusiness.run {
                    getCartoonPage()
                }.toDataState()
                tempPage = tempPage.copy(
                    pageList = pageListState.map {
                        it.map { page ->
                            TabState.Page(pageBusiness, page)
                        }
                    }
                )
                updatePageState()
            }
        }
    }

    fun onSourceChange(sourceKey: String) {
        viewModelScope.launch {
            selectionSourceKeyPref.set(sourceKey)
        }
    }

    fun onPageSelected(tabState: TabState) {
        when (tabState) {
            is TabState.Discover -> {
                update {
                    it.copy(
                        pageState = it.pageState.copy(
                            selection = -1,
                            isSelectedDiscover = true,
                        )
                    )
                }
            }

            is TabState.Page -> {
                update {
                    it.copy(
                        pageState = it.pageState.copy(
                            selection = it.pageState.showTabList.indexOf(tabState),
                            isSelectedDiscover = false,
                        )
                    )
                }
            }
        }
    }

    fun showSourceChangePopup() {
        update {
            it.copy(
                popup = Popup.SourceChange(sourceBundle)
            )
        }
    }


}