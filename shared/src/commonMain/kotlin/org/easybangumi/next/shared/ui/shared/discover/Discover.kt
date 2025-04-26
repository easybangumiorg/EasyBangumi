package org.easybangumi.next.shared.ui.shared.discover

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverColumnJumpRouter
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
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
 *
 *  发现页模块，列表，从上到下排列
 *  【banner】from DiscoverComponent
 *  【history】from HistoryDatabase
 *  【column1】from DiscoverComponent
 *  【column2】from DiscoverComponent
 *  【column3】from DiscoverComponent
 */
@Composable
fun Discover(
    discoverBusiness: ComponentBusiness<DiscoverComponent>,

    // 跳转详情页
    onJumpDetail: (CartoonIndex) -> Unit,

    // 发现页 【查看更多】区域点击跳转
    onJumpRouter: (DiscoverColumnJumpRouter) -> Unit,
) {
    val viewModel = vm(::DiscoverViewModel, discoverBusiness)

    val uiState = viewModel.ui.value
    LoadScaffold(Modifier, data = uiState.banner) {}

}