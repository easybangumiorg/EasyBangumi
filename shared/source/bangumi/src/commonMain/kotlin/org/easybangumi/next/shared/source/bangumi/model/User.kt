package org.easybangumi.next.shared.source.bangumi.model

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
 */
/**
 * {
 *     "avatar": {
 *         "large": "https://lain.bgm.tv/pic/user/l/000/84/85/848551.jpg?r=1743938195&hd=1",
 *         "medium": "https://lain.bgm.tv/r/200/pic/user/l/000/84/85/848551.jpg?r=1743938195&hd=1",
 *         "small": "https://lain.bgm.tv/r/100/pic/user/l/000/84/85/848551.jpg?r=1743938195&hd=1"
 *     },
 *     "sign": "",
 *     "url": "https://bgm.tv/user/848551",
 *     "username": "848551",
 *     "nickname": "heyanle",
 *     "id": 848551,
 *     "user_group": 10,
 *     "reg_time": "2024-01-08T00:45:20+08:00",
 *     "email": "1371735400@qq.com",
 *     "time_offset": 8
 * }
 */
@Serializable
data class User (
    @SerialName("id") val id: Long,
    @SerialName("username") val username: String,
    @SerialName("nickname") val nickname: String,
    @SerialName("avatar") val avatar: BgmImages?,
    @SerialName("sign") val sign: String?,
    @SerialName("url") val url: String?,
    @SerialName("user_group") val userGroup: Int?,
    @SerialName("reg_time") val regTime: String?,
    @SerialName("email") val email: String?,
    @SerialName("time_offset") val timeOffset: Int?,
)