package org.easybangumi.next.shared.bangumi.data.repository

import kotlinx.coroutines.CoroutineScope
import okio.BufferedSink
import okio.BufferedSource
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.store.repository.FileAbsRepository
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.bangumi.BgmSubject
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
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
 */
class BangumiSubjectRepository(
    folder: UFD,
    private val cartoonIndex: CartoonIndex,
    val bangumiDetailBusiness: ComponentBusiness<BangumiDetailComponent>,
    scope: CoroutineScope,
): FileAbsRepository<BgmSubject>(folder, "subject.json", scope) {


    override suspend fun fetchRemoteData(): DataState<BgmSubject> {
        return bangumiDetailBusiness.run {
            getSubject(cartoonIndex)
        }
    }

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
}