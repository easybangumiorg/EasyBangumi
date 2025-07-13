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
import org.easybangumi.next.shared.source.bangumi.model.BgmPerson
import org.easybangumi.next.shared.source.bangumi.model.BgmSubject

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
// TODO cache
class BangumiSubjectRepository(
    val subjectId: String,
    val api: BangumiApi,
    val config: BangumiConfig,
    val scope: CoroutineScope,
): DataRepository<BgmSubject> {

    private val _flow = MutableStateFlow<DataState<BgmSubject>>(DataState.loading())
    override val flow: StateFlow<DataState<BgmSubject>>
        get() = _flow


    override fun refresh(): Boolean {
        scope.launch {
            val res = api.getSubject(subjectId).await().toDataState()
            _flow.update { res }
        }
        return true
    }

}