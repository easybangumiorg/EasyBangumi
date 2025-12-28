package org.easybangumi.next.shared.source.api.utils

/**
 * Created by heyanlin on 2025/7/18.
 */
interface HttpHelper {

    data class Response(
        val status: Int,
        val headers: Map<String, List<String>>,
        val body: String,
    )

    suspend fun get(
        url: String,
        // 为空则使用默认
        userAgent: String? = null,
        headers: Map<String, List<String>> = emptyMap()
    ): Response

    suspend fun postJson(
        url: String,
        // 为空则使用默认
        userAgent: String? = null,
        headers: Map<String, List<String>> = emptyMap(),
        body: String,
    ): Response

    suspend fun postFormBody(
        url: String,
        // 为空则使用默认
        userAgent: String? = null,
        headers: Map<String, List<String>> = emptyMap(),
        body: Map<String, String>,
    ): Response



}