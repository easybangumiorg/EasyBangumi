package org.easybangumi.next.shared.data.bangumi

import com.mayakapps.kache.ContainerKache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.BufferedSink
import okio.BufferedSource
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.store.repository.KacheAbsRepository
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.bangumi.model.BgmCharacter
import org.easybangumi.next.shared.source.bangumi.model.BgmPerson
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
class BangumiCharacterListRepository (
    val cartoonIndex: CartoonIndex,
    val bangumiDetailBusiness: ComponentBusiness<BangumiDetailComponent>,
    subjectKache: ContainerKache<String, String>?,
    scope: CoroutineScope,
): KacheAbsRepository<List<BgmCharacter>>(
    cacheKey = cartoonIndex.id,
    subjectKache = subjectKache,
    scope = scope,
) {

    override fun save(data: List<BgmCharacter>, sink: BufferedSink) {
        data.forEach {
            val json = jsonSerializer.serialize(it)
            if (json.isNotEmpty()) {
                sink.writeUtf8(json)
                sink.writeUtf8("\n")
            }
        }
    }

    override fun load(source: BufferedSource): List<BgmCharacter>? {
        val list = mutableListOf<BgmCharacter>()

        var text = source.readUtf8Line()
        while(text != null) {
            if (text.isNotEmpty()) {
                val obj = jsonSerializer.deserialize(text, BgmCharacter::class, null)
                if (obj != null) {
                    list.add(obj)
                }
            }
            text = source.readUtf8Line()
        }
        if (list.isEmpty()) {
            return null
        }
        return list
    }

    override suspend fun fetchRemoteData(): DataState<List<BgmCharacter>> {
        return bangumiDetailBusiness.run {
            getCharacterList(cartoonIndex)
        }
    }
}