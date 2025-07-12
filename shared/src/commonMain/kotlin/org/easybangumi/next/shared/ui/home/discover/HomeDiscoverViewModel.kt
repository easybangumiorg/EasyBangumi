package org.easybangumi.next.shared.ui.home.discover
//
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.flow.distinctUntilChanged
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.launch
//import org.easybangumi.next.lib.store.preference.PreferenceStore
//import org.easybangumi.next.shared.foundation.view_model.StateViewModel
//import org.easybangumi.next.shared.plugin.api.component.ComponentBundle
//import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
//import org.easybangumi.next.shared.plugin.core.component.SimpleComponentBundle
//import org.easybangumi.next.shared.plugin.api.component.ComponentBusiness
//import org.easybangumi.next.shared.plugin.core.info.SourceInfo
//import org.easybangumi.next.shared.plugin.core.source.SourceBundle
//import org.easybangumi.next.shared.plugin.core.source.SourceController
//import org.koin.core.component.inject
//
///**
// *    https://github.com/easybangumiorg/EasyBangumi
// *
// *    Copyright 2025 easybangumi.org and contributors
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// */
//
//class HomeDiscoverViewModel(
//    private val sourceBundle: SourceBundle
//): StateViewModel<HomeDiscoverViewModel.State>(State(sourceBundle)) {
//
//    private val preferenceStore: PreferenceStore by inject()
//    private val sourceController: SourceController by inject()
//
//    private val selectionSourceKeyPref = preferenceStore.getString("selection_source_key", "")
//
//
//    sealed class Popup {
//        data class SourceChange(
//            val sourceBundle: SourceBundle,
//        ): Popup()
//    }
//
//    data class State(
//        val sourceBundle: SourceBundle,
//        val selectionSourceKey: String = "",
//        val discoverBusiness: ComponentBusiness<DiscoverComponent<*>>? = null,
//        val popup: Popup? = null,
//    ) {
//        val realSelectionKey: String? by lazy {
//            if (sourceBundle.contains(selectionSourceKey)) selectionSourceKey else sourceBundle.keys().firstOrNull()
//        }
//
//        val componentBundle: ComponentBundle? by lazy {
//            realSelectionKey?.let { sourceBundle.componentBundle(it) }
//        }
//
//        val sourceInfo: SourceInfo.Loaded? by lazy {
//            realSelectionKey?.let { sourceBundle.sourceInfo(it) }
//        }
//
//    }
//
//
//    init {
//        // 1. source key pref
//        viewModelScope.launch {
//            selectionSourceKeyPref.flow().collectLatest { key ->
//                update {
//                    it.copy(
//                        selectionSourceKey = key
//                    )
//                }
//            }
//        }
//
//
//        // 2. load page
//        viewModelScope.launch {
//            state.map { it.componentBundle }.distinctUntilChanged().collectLatest { bundle ->
//                if (bundle == null) {
//                    update {
//                        it.copy(discoverBusiness = null)
//                    }
//                    return@collectLatest
//                }
//
//
//
//                val discoverComponent = bundle.getBusiness(DiscoverComponent::class)
//                update {
//                    it.copy(discoverBusiness = discoverComponent)
//                }
//            }
//        }
//    }
//
//    fun onSourceChange(sourceKey: String) {
//        viewModelScope.launch {
//            selectionSourceKeyPref.set(sourceKey)
//        }
//    }
//
//    fun showSourceChangePopup() {
//        update {
//            it.copy(
//                popup = Popup.SourceChange(sourceBundle)
//            )
//        }
//    }
//
//
//}