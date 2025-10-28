package org.easybangumi.next.shared.bangumi.account

import kotlinx.coroutines.flow.StateFlow
import org.easybangumi.next.lib.store.file_helper.json.JsonFileHelper
import org.easybangumi.next.lib.utils.pathProvider

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
class BangumiAccountController {

    private val jsonHelper: JsonFileHelper<BangumiAccountInfo> by lazy {
        JsonFileHelper(
            pathProvider.getFilePath("Bangumi"),
            "BangumiAccount",
            BangumiAccountInfo::class,
            BangumiAccountInfo.EMPTY,
        )
    }

    data class BangumiAccountInfo(
        val token: String,
        val username: String
    ) {
        companion object {
            val EMPTY = BangumiAccountInfo("", "")
        }
    }


    val flow : StateFlow<BangumiAccountInfo> by lazy {
        jsonHelper.flow()
    }
}