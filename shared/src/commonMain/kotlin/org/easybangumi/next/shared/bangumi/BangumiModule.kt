package org.easybangumi.next.shared.bangumi

import org.easybangumi.next.shared.bangumi.account.BangumiAccountController
import org.easybangumi.next.shared.bangumi.data.BangumiDataController
import org.koin.dsl.module

/**
 * Created by heyanlin on 2025/11/5.
 */
val bangumiModule = module {
    single {
        BangumiAccountController(get())
    }
    single {
        BangumiDataController(get(), get())
    }
}