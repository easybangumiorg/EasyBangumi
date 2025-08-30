package org.easybangumi.next.shared.ui.detail.preview

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.case.BangumiCase
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.bangumi.model.BgmSubject
import org.easybangumi.next.shared.source.case.DetailSourceCase
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
class BangumiDetailPreviewViewModel(
    private val cartoonIndex: CartoonIndex,
): StateViewModel<DataState<BgmSubject>>(DataState.none()) {

    val bangumiCase: BangumiCase by inject()

    val coverUrl  = bangumiCase.coverUrl(cartoonIndex)

    val repository = bangumiCase.getSubjectRepository(cartoonIndex)



    init {
        repository.refreshIfNone()
        viewModelScope.launch {
            repository.flow.collectLatest {state ->
                update {
                    state
                }
            }
        }

    }


}