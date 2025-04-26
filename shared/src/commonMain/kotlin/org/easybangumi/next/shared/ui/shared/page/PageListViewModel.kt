package org.easybangumi.next.shared.ui.shared.page

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.foundation.view_model.LogicUIViewModel
import org.easybangumi.next.shared.foundation.view_model.parent.ParentViewModel
import org.easybangumi.next.shared.foundation.view_model.parent.ParentViewModelDelegate
import org.easybangumi.next.shared.plugin.api.component.page.CartoonPage
import org.easybangumi.next.shared.plugin.api.component.page.PageComponent
import org.easybangumi.next.shared.plugin.api.toDataState
import org.easybangumi.next.shared.plugin.core.component.ComponentBusiness

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
class PageListViewModel(
    private val pageBusiness: ComponentBusiness<PageComponent>
): LogicUIViewModel<PageListViewModel.State, PageListViewModel.State>(),
    ParentViewModel<CartoonPage> by ParentViewModelDelegate()
{

    data class State(
        val pageList: DataState<List<CartoonPage>> = DataState.none()
    )

    override val initUiState: PageListViewModel.State get() = State()
    override val initLogicState: PageListViewModel.State get() = State()
    override suspend fun logicToUi(logicState: PageListViewModel.State): PageListViewModel.State {
        return logicState
    }

    init {
        viewModelScope.launch {
            loadPageList()
        }
    }

    suspend fun loadPageList() {
        val res = pageBusiness.run {
            getCartoonPage().toDataState()
        }
        update { it.copy(res) }
    }

    override fun onCleared() {
        super.onCleared()
        clearChildren()
    }

}