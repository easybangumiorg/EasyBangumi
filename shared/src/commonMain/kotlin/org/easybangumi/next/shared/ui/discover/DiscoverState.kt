package org.easybangumi.next.shared.ui.discover

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.PagerState
import org.easybangumi.next.shared.foundation.scroll_header.DiscoverScrollHeaderTabState
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderState

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
class DiscoverState(
    val scrollHeaderTabState: ScrollableHeaderState,
    val pagerState: PagerState,
) {

    val discoverScrollHeaderTabStateList = listOf<DiscoverScrollHeaderTabState>()

    fun getScrollableHeaderState(index: Int): DiscoverScrollHeaderTabState? {
        return discoverScrollHeaderTabStateList[index]
    }

}