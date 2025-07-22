package org.easybangumi.next.shared.source.api.component.play

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.EasyPagingSource
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.shared.data.cartoon.CartoonPlayCover

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
class PlayCoverPagingSource(
    private val playComponent: PlayComponent,
    private val searchParam: IPlayComponent.PlayLineSearchParam,
): EasyPagingSource<CartoonPlayCover> {

    override val initKey: String = playComponent.getFirstKey()

    override suspend fun load(key: String): DataState<PagingFrame<CartoonPlayCover>> {
        return playComponent.searchPlayCovers(
            searchParam,
            key
        )
    }
}

fun PlayComponent.createSearchPlayPagingSource(
    param: IPlayComponent.PlayLineSearchParam,
): EasyPagingSource<CartoonPlayCover> {
    return PlayCoverPagingSource(this, param)
}