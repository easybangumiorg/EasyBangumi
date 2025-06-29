package org.easybangumi.shared.plugin.bangumi

import org.easybangumi.shared.plugin.bangumi.business.BangumiApi
import org.easybangumi.shared.plugin.bangumi.business.BangumiBusiness
import org.koin.dsl.binds
import org.koin.dsl.module

/**
 * Created by heyanle on 2025/6/27.
 */
val bangumiModule = module {
    single {
        BangumiBusiness(get())
    }

    single {
        get<BangumiBusiness>().api
    } .binds(arrayOf(BangumiApi::class))

}