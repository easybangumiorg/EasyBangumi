package org.easybangumi.next.shared.data.bangumi

import com.mayakapps.kache.ContainerKache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import okio.BufferedSink
import okio.BufferedSource
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.store.repository.KacheAbsRepository
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.utils.DataRepository
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.bangumi.model.BgmSubject
import org.easybangumi.next.shared.source.bangumi.source.BangumiDetailComponent

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
 *  1. 进 Bangumi 详情页会触发一次刷新
 */
class BangumiSubjectRepository(
    val cartoonIndex: CartoonIndex,
    val bangumiDetailBusiness: ComponentBusiness<BangumiDetailComponent>,
    subjectKache: ContainerKache<String, String>?,
    scope: CoroutineScope,
): KacheAbsRepository<BgmSubject>(
    cacheKey = cartoonIndex.id,
    subjectKache = subjectKache,
    scope = scope,
) {

    override fun save(data: BgmSubject, sink: BufferedSink) {
        val json = jsonSerializer.serialize(data)
        logger.info(json)
        if (json.isNotEmpty()) {
            sink.writeUtf8(json)
        }
    }

    override fun load(source: BufferedSource): BgmSubject? {
        val text = source.readUtf8()
        return jsonSerializer.deserialize(text, BgmSubject::class, null)
    }

    override suspend fun fetchRemoteData(): DataState<BgmSubject> {
        return bangumiDetailBusiness.run {
            getSubject(cartoonIndex)
        }
    }
}