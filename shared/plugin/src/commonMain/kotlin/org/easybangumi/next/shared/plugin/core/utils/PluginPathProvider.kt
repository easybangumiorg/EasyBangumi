package org.easybangumi.next.shared.plugin.core.utils

import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.plugin.api.source.Source

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

object PluginPathProvider {

    fun getExtensionWorkPath(): UFD {
        return pathProvider.getFilePath("extension")
    }

    fun getExtensionCachePath(): UFD {
        return pathProvider.getCachePath("extension")
    }

    fun getSourceControllerWorkPath(): UFD {
        return pathProvider.getFilePath("source")
    }

    fun getSourceWorkPath(source: Source): UFD {
        val sourceController = getSourceControllerWorkPath()
        return UniFileFactory.fromUFD(sourceController)?.createDirectory(source.key)?.getUFD()
            ?: throw IllegalStateException("Failed to create source work path")
    }


}