package org.easybangumi.next.shared.data.bangumi

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
sealed class BgmCollectResp {
    data class BgmCollectData(
        val data: BgmCollect,
    ): BgmCollectResp()

    data object BgmCollectNone: BgmCollectResp()

    fun dataOrNull(): BgmCollect? {
        return when (this) {
            is BgmCollectData -> this.data
            is BgmCollectNone -> null
        }
    }
}