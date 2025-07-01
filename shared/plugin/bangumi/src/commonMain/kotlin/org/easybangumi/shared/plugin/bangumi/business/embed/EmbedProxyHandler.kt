package org.easybangumi.shared.plugin.bangumi.business.embed

import io.ktor.client.plugins.api.TransformResponseBodyContext
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel

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


interface EmbedProxyHandler {

    fun onReq(
        builder: HttpRequestBuilder
    ): Boolean

    suspend fun onResp(
        context: TransformResponseBodyContext,
        response: HttpResponse,
        content: ByteReadChannel,
        requestedType: TypeInfo
    ): Any?

}