package org.easybangumi.next.shared.data.bangumi

import org.easybangumi.next.shared.data.cartoon.CartoonTag

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
object BangumiConst {

    const val BANGUMI_SOURCE_KEY = "bangumi"

    data class BangumiCollectType(
        val type: Int,
        val label: String,
    )

    val collectTypeList = listOf(
        BangumiCollectType(1, "想看"),
        BangumiCollectType(2, "在看"),
        BangumiCollectType(3, "看过"),
        BangumiCollectType(4, "搁置"),
        BangumiCollectType(5, "抛弃"),
    )

    fun getTypeDataById(type: Int): BangumiConst.BangumiCollectType? {
        return collectTypeList.getOrNull(type - 1)
    }

}