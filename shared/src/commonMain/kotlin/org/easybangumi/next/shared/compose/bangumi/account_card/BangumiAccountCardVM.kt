package org.easybangumi.next.shared.compose.bangumi.account_card

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.bangumi.data.BangumiUserDataProvider
import org.easybangumi.next.shared.case.BangumiCase
import org.easybangumi.next.shared.data.bangumi.User
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.koin.core.component.inject

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
class BangumiAccountCardVM: StateViewModel<DataState<User>>(DataState.none()) {

    private val bangumiCase: BangumiCase by inject()
    private val bgmUserDataProvider = bangumiCase.flowUserDataProvider()

    init {
        viewModelScope.launch() {
            bgmUserDataProvider.flatMapLatest { provider ->
                provider?.getUserRepository()?.flow ?: flow { emit(DataState.none()) }
            }.collectLatest {  user ->
                update { user }
            }
        }
    }

}