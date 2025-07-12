package org.easybangumi.next.shared.ui.home.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.ui.discover.bangumi.BangumiDiscover
import org.easybangumi.next.shared.ui.discover.bangumi.BangumiDiscoverTopAppBar
import org.easybangumi.next.shared.ui.discover.bangumi.BangumiDiscoverViewModel

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
@Composable
fun HomeDiscover() {
    val vm = vm(::BangumiDiscoverViewModel)
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        BangumiDiscoverTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            vm = vm,
            scrollBehavior = behavior
        )
        BangumiDiscover(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background),
            vm = vm,
            nestedScrollConnection = behavior.nestedScrollConnection,
            onCoverClick = {},
            onTimelineClick = {}
        )
    }


}
