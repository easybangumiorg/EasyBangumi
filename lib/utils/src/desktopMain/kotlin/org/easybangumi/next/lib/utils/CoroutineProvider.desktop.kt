package org.easybangumi.next.lib.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

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

private class CoroutineProviderImpl : CoroutineProvider {
    override fun io(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    override fun single(): CoroutineDispatcher {
        TODO()
    }

    override fun newSingle(name: String): CoroutineDispatcher {
        TODO()
    }

    override fun main(): CoroutineDispatcher {
        return Dispatchers.Main
    }
}

actual val coroutineProvider: CoroutineProvider by lazy {
    CoroutineProviderImpl()
}