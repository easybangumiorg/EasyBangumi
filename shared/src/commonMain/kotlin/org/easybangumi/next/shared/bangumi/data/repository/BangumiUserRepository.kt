package org.easybangumi.next.shared.bangumi.data.repository

import kotlinx.coroutines.CoroutineScope
import okio.BufferedSink
import okio.BufferedSource
import org.easybangumi.next.lib.serialization.deserialize
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.serialization.serialize
import org.easybangumi.next.lib.store.repository.FileAbsRepository
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.bangumi.account.BangumiAccountController
import org.easybangumi.next.shared.data.bangumi.BgmPerson
import org.easybangumi.next.shared.data.bangumi.BgmSubject
import org.easybangumi.next.shared.data.bangumi.User
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi

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
class BangumiUserRepository(
    folder: UFD,
    scope: CoroutineScope,
    private val bangumiApi: BangumiApi,
    private var accountInfo: BangumiAccountController.BangumiAccountInfo,
): FileAbsRepository<User>(folder, "user.jsonl", scope) {
    override suspend fun fetchRemoteData(): DataState<User> {
        return bangumiApi.getMe(accountInfo.token).await().toDataState()
    }

    override fun save(data: User, sink: BufferedSink) {
        val json = jsonSerializer.serialize(data)
        logger.info(json)
        if (json.isNotEmpty()) {
            sink.writeUtf8(json)
        }
    }

    override fun load(source: BufferedSource): User? {
        val text = source.readUtf8()
        return jsonSerializer.deserialize(text, User::class, null)
    }
}