package org.easybangumi.next.shared.download.platform.android

import org.easybangumi.next.shared.download.action.DownloadActionRegistry
import org.easybangumi.next.shared.download.action.DownloadChain
import org.easybangumi.next.shared.download.action.DownloadAction
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android 平台下载模块
 * 注册 AriaM3u8DownloadAction 和 TransformerAction
 */
val androidDownloadModule: Module = module {
    single { AriaM3u8DownloadAction() }
    single { TransformerAction() }
}

/**
 * 注册 Android 平台特有的下载动作
 */
fun registerAndroidDownloadActions() {
    DownloadActionRegistry.register(AriaM3u8DownloadAction())
    DownloadActionRegistry.register(TransformerAction())
}
