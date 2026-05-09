package org.easybangumi.next.shared.download.platform.desktop

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Desktop 平台下载模块
 */
val desktopDownloadModule: Module = module {
    single { FfmpegM3u8DownloadAction(get(), get()) }
}
