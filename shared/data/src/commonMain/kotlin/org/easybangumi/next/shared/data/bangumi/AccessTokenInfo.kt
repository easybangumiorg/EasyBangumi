package org.easybangumi.next.shared.data.bangumi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
 * {
 *     "access_token":"YOUR_ACCESS_TOKEN",
 *     "expires_in":604800,
 *     "token_type":"Bearer",
 *     "scope":null,
 *     "refresh_token":"YOUR_REFRESH_TOKEN"
 *     "user_id" : USER_ID
 * }
 */
@Serializable
data class AccessTokenInfo(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String,
    @SerialName("scope") val scope: String?,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("user_id") val userId: Int
)