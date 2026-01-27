package org.easybangumi.next.shared.compose.bangumi.comment

import androidx.lifecycle.viewModelScope
import app.cash.paging.cachedIn
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.shared.data.bangumi.BgmReviews
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.SourceCase
import org.koin.core.component.inject
import kotlin.getValue

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
class BangumiCommentVM(
    private val cartoonIndex: CartoonIndex,
): StateViewModel<BangumiCommentVM.State>(State()) {

    data class State(
        val commentPaging: PagingFlow<BgmReviews>? = null
    )

    val sourceCase: SourceCase by inject()
    val bangumiDetailBusiness = sourceCase.getBangumiDetailBusiness()

    init {
       refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val pagingSource = bangumiDetailBusiness.runDirect { createCommentPagingSource(cartoonIndex) }
            val pagingFlow = pagingSource.newPagingFlow().cachedIn(viewModelScope)
            update {
                it.copy(
                    commentPaging = pagingFlow
                )
            }
        }
    }
}