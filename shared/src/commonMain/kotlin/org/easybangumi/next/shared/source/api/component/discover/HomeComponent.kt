package org.easybangumi.next.shared.source.api.component.discover

import org.easybangumi.next.lib.utils.DataState


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
 * HomeComponent 是 DiscoverComponent 的子集，是通用模板的发现页
 */

interface IHomeComponent {

    suspend fun getMainTab(): DataState<List<HomeMainTab>>

    suspend fun getSubTab(mainTab: HomeMainTab.GroupMainTab): DataState<List<HomeSubTab>>

    suspend fun getTabContent(
        mainTab: HomeMainTab,
        subTab: HomeSubTab?,
    ): DataState<HomePage>



}


interface HomeComponent: IHomeComponent, DiscoverComponent