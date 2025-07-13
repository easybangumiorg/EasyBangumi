package org.easybangumi.next.shared.ui.home.discover

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.foundation.scroll_header.DiscoverScrollHeaderBehavior
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderBehavior
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderState
import org.easybangumi.next.shared.foundation.scroll_header.rememberScrollableHeaderState
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.ui.discover.bangumi.BangumiDiscover
import org.easybangumi.next.shared.ui.discover.bangumi.BangumiDiscoverTopAppBar
import org.easybangumi.next.shared.ui.discover.bangumi.BangumiDiscoverViewModel
import kotlin.math.absoluteValue

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
//    val behavior = TopAppBarDefaults.pinnedScrollBehavior()

    val nav = LocalNavController.current
    val scrollableHeaderState: ScrollableHeaderState = rememberScrollableHeaderState()
    val scrollHeaderBehavior: DiscoverScrollHeaderBehavior = ScrollableHeaderBehavior.discoverScrollHeaderBehavior(
        state = scrollableHeaderState
    )

    var isHeaderPinned by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        snapshotFlow {
            scrollableHeaderState.offset.absoluteValue >= scrollableHeaderState.headerHeight.value - 20
        }.collect {
            isHeaderPinned = it
        }
    }

    val topAppBarBackgroundColor by animateColorAsState(
        targetValue = if (scrollableHeaderState.offset != 0f)
            MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        label = "background color")

    val backgroundColor by animateColorAsState(
        targetValue = if (isHeaderPinned)
            MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        label = "background color")
    Column(
        modifier = Modifier.fillMaxSize()
            .background(topAppBarBackgroundColor),
    ) {
        BangumiDiscoverTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            vm = vm,
        )
        BangumiDiscover(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)),
//                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            vm = vm,
            scrollHeaderBehavior = scrollHeaderBehavior,
            headerContainerColor = backgroundColor,
            pinHeaderContainerColor = backgroundColor,
            contentContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            onCoverClick = {
                val page = RouterPage.Detail.fromCartoonIndex(it.toCartoonIndex())
                nav.navigate(page)
            },
            onTimelineClick = {}
        )
    }


}
