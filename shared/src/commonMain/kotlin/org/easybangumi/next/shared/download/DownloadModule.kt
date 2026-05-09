package org.easybangumi.next.shared.download

import org.easybangumi.next.shared.download.action.CopyAndNfoAction
import org.easybangumi.next.shared.download.action.DownloadActionRegistry
import org.easybangumi.next.shared.download.action.KtorHttpDownloadAction
import org.easybangumi.next.shared.download.action.ParseAction
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * 通用下载模块（跨平台）
 */
val downloadModule: Module = module {
    single { ParseAction(get()) }
    single { KtorHttpDownloadAction(get(), get()) }
    single { CopyAndNfoAction(get()) }

    single { DownloadReqController(get()) }
    single { DownloadDispatcher() }
    single { DownloadManager(get(), get(), get(), get()) }
}
