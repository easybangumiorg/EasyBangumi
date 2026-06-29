package org.easybangumi.next.lib.utils

import org.easybangumi.next.lib.unifile.UFD
import org.koin.mp.KoinPlatform

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

expect interface PathProvider {

    fun getFilePath(type: String): UFD

    fun getCachePath(type: String): UFD

}

val pathProvider: PathProvider
    get() = KoinPlatform.getKoin().get<PathProvider>()


