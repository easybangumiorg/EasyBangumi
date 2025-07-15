package org.easybangumi.next.kcef

import dev.datlag.kcef.KCEFBuilder
import kotlinx.coroutines.CoroutineDispatcher
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider
import org.slf4j.Logger

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

class KcefServiceImpl: KcefService {

    override fun onKcefInit(builder: KCEFBuilder): KCEFBuilder {
        builder.addArgs(
            "-Dcef.logging.enabled=true",
            "-Dcef.log.severity=verbose",
            "-Dcef.log.file=cef.log",
            "--autoplay-policy=no-user-gesture-required",
            "--mute-audio"
        )

        builder.settings {
            logSeverity = KCEFBuilder.Settings.LogSeverity.Verbose
            windowlessRenderingEnabled = true
        }
        return builder
    }

    override fun getSingletonDispatcher(): CoroutineDispatcher {
        return coroutineProvider.newSingle()
    }

    override fun getLogger(tag: String): Logger {
        return logger(tag).getSlf4jLogger()
    }
}