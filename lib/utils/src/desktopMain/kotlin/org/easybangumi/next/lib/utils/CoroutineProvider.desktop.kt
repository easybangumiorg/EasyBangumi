package org.easybangumi.next.lib.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

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

    private val singleThreadExecutor: CoroutineDispatcher by lazy {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }

    private val singleThreadExecutorMap = safeMutableMapOf<String, CoroutineDispatcher>()


    override fun io(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    override fun single(): CoroutineDispatcher {
        return singleThreadExecutor
    }

    override fun newSingle(): CoroutineDispatcher {
        return Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }

    override fun main(): CoroutineDispatcher {
        return Dispatchers.Main
    }

    override fun singleForTag(tag: String): CoroutineDispatcher {
        return singleThreadExecutorMap.getOrPut(tag) {
            Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        }
    }
}

actual val coroutineProvider: CoroutineProvider by lazy {
    CoroutineProviderImpl()
}