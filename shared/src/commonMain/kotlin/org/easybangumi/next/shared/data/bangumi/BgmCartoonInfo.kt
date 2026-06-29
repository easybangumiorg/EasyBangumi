package org.easybangumi.next.shared.data.bangumi

import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.CartoonInfo

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
 *  bangumi 番剧和本地番剧信息总和
 */
data class BgmCartoonInfo(
    val index: CartoonIndex,
    // local info
    val cartoonInfo: CartoonInfo? = null,

    // bangumi info
    val trendsSubject: BgmTrendsSubject? = null,
    val subject: BgmSubject? = null,
    val collection: BgmCollect? = null
)