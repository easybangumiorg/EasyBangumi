package org.easybangumi.next.vlcj

import org.easybangumi.next.lib.logger.logger
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryProviderPriority
import java.io.File

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
class AppResourceDirectoryProvider: DiscoveryDirectoryProvider {

    private val logger = logger()

    override fun priority(): Int {
        return DiscoveryProviderPriority.USER_DIR
    }

    override fun directories(): Array<out String?>? {
        logger.info("AppResourceDirectoryProvider directories called")
        return arrayOf(
//            "C:\\Program Files\\VideoLAN\\VLC"
            File(File(System.getProperty("compose.application.resources.dir"), "vlc"), "lib").absolutePath
        ).apply {
            logger.info("AppResourceDirectoryProvider directories: ${this.joinToString(", ")}")
        }
    }

    override fun supported(): Boolean {
        return true
    }
}