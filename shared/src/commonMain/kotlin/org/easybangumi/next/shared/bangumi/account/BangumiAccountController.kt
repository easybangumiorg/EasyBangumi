package org.easybangumi.next.shared.bangumi.account

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlinx.serialization.Serializable
import org.easybangumi.next.lib.store.file_helper.json.JsonFileHelper
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.data.bangumi.AccessTokenInfo
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

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
 *  Bangumi 登录态保持 - token
 */
class BangumiAccountController(
    private val bangumiApi: BangumiApi,
) {

    private val jsonHelper: JsonFileHelper<BangumiAccountInfo> by lazy {
        JsonFileHelper(
            pathProvider.getFilePath("Bangumi"),
            "BangumiAccount",
            BangumiAccountInfo::class,
            BangumiAccountInfo.EMPTY,
            true,
        )
    }

    @Serializable
    data class BangumiAccountInfo(
        val token: String,
        val username: String,
        // 如果为用户手动输入，则为 null
        val accessTokenInfo: AccessTokenInfo? = null,
    ) {
        companion object {
            val EMPTY = BangumiAccountInfo("", "", null)
        }
    }


    val accountInfoFlow : StateFlow<BangumiAccountInfo> by lazy {
        jsonHelper.flow()
    }

    fun updateAccountInfo(accountInfo: BangumiAccountInfo) {
        jsonHelper.push(accountInfo)
    }

    fun updateAccessToken(accessTokenInfo: AccessTokenInfo) {
        val info = accountInfoFlow.value
        updateAccountInfo(
            BangumiAccountInfo(
                token = accessTokenInfo.accessToken,
                username = accessTokenInfo.userId.toString(),
                accessTokenInfo = accessTokenInfo,
            )
        )
    }


    suspend fun checkRefreshIfNeed() {
        val info = accountInfoFlow.value
        val accessTokenInfo = info.accessTokenInfo ?: return
        val now = Clock.System.now().toEpochMilliseconds()
        // 提前 6h 刷新
        if (now >= info.accessTokenInfo.expiresIn - 6.hours.inWholeMilliseconds) {
            if (!tryRefresh(accessTokenInfo)) {
                // 刷新失败且已过期，清除登录态
                if (now >= info.accessTokenInfo.expiresIn) {
                    updateAccountInfo(BangumiAccountInfo.EMPTY)
                }
                // TODO 提示用户重新登录
            }

        }
    }

    suspend fun tryRefresh(
        accessTokenInfo: AccessTokenInfo? = accountInfoFlow.value.accessTokenInfo,
    ): Boolean {
        accessTokenInfo ?: return false
        val info = bangumiApi.refreshAccessToken(accessTokenInfo.refreshToken)
            .await()
            .onFailure {
                // TODO 提示用户刷新失败
            }.onSuccess {
                updateAccessToken(it)
            }
        return info.isSuccess
    }


}