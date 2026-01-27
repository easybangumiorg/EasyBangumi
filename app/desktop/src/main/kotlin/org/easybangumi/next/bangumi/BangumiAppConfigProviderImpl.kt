package org.easybangumi.next.bangumi

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.easybangumi.next.EasyConfig
import org.easybangumi.next.shared.bangumi.account.BangumiAccountController
import org.easybangumi.next.shared.source.bangumi.BangumiAppConfig
import org.easybangumi.next.shared.source.bangumi.BangumiAppConfigProvider
import org.easybangumi.next.shared.source.bangumi.BangumiConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

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
class BangumiAppConfigProviderImpl: BangumiAppConfigProvider {

    override fun provide(): BangumiAppConfig {
        return BangumiAppConfig(
            appId = EasyConfig.BANGUMI_APP_ID,
            appSecret = EasyConfig.BANGUMI_APP_SECRET,
            callbackUrl = EasyConfig.BANGUMI_APP_CALLBACK_URL,
            handler = BangumiHandler()
        )
    }

    class BangumiHandler: KoinComponent, BangumiConfig.BangumiHandler {

        val scope = MainScope()

        val bangumiAccountController: BangumiAccountController by inject()
        override fun onAuthFailed() {
            scope.launch {
                bangumiAccountController.tryRefresh()
            }
        }
    }
}