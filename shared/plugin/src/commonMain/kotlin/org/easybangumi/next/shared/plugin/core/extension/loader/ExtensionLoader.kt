package org.easybangumi.next.shared.plugin.core.extension.loader

import org.easybangumi.next.shared.plugin.extension.ExtensionManifest
import org.easybangumi.next.shared.plugin.info.ExtensionInfo

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

interface ExtensionLoader {

    fun loadType(): Int

    fun canLoad(extensionManifest: ExtensionManifest): Boolean {
        return extensionManifest.loadType == loadType()
    }

    suspend fun load(extensionManifest: ExtensionManifest): ExtensionInfo

}