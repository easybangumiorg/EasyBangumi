package org.easybangumi.next.shared.source.bangumi.source.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataRepository
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.source.bangumi.BangumiConfig
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.easybangumi.next.shared.source.bangumi.model.BgmCharacter

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

class BangumiCharacterRepository(
    val subjectId: String,
    val api: BangumiApi,
    val config: BangumiConfig,
    val scope: CoroutineScope,
) : DataRepository<List<BgmCharacter>> {

    private val _flow = MutableStateFlow<DataState<List<BgmCharacter>>>(DataState.loading())
    override val flow: StateFlow<DataState<List<BgmCharacter>>>
        get() = _flow

    override fun refresh(): Boolean {
        scope.launch {
            val res = api.getCharacterList(subjectId).await().toDataState()
            _flow.update { res }
        }
        return true
    }

}