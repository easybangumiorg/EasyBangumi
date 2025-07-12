package org.easybangumi.ext.shared.plugin.bangumi.plugin

import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.SourceException
import org.easybangumi.next.shared.plugin.api.component.BaseComponent
import org.easybangumi.next.shared.plugin.api.component.discover.common.BannerHeadline
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.api.component.discover.common.RecommendTab
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.easybangumi.next.shared.source.bangumi.business.BangumiBusiness
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp.Success
import org.easybangumi.next.shared.source.bangumi.model.toCartoonCover
import org.koin.core.component.inject

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
class BangumiDiscoverComponent: DiscoverComponent<BangumiDiscoverManager>, BaseComponent() {

    private val logger = logger()
    private val api: BangumiApi by inject()

    private val _manager: BangumiDiscoverManager by lazy {
        BangumiDiscoverManager(api)
    }

    override fun getManager(): BangumiDiscoverManager {
        return _manager
    }


    private fun <T> BgmRsp<T>.getOrThrow(): T {
        return when (this) {
            is Success -> data
            is BgmRsp.Error -> throw SourceException("业务错误：${code} ${url} ${throwable?.message ?: throwable?.let { it::class.simpleName }}", throwable)
        }
    }
}