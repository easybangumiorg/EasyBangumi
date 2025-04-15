package org.easybangumi.next.shared.plugin.core.extension.provider

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.shared.plugin.extension.ExtensionException

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
object ProviderFactory {

    private val looger = logger()

    class Result(
        val provider: List<ExtensionProvider>,
        val exception: List<ExtensionException>,
    )

    fun createProvider(
        workerFile: UFD,
        cacheFile: UFD,

        scope: CoroutineScope,
        singleDispatcher: CoroutineDispatcher,
    ): Result {

        val providerList = arrayListOf<ExtensionProvider>()
        val exceptionList = arrayListOf<ExtensionException>()

        val workerFile = UniFileFactory.fromUFD(workerFile)
        val cacheFile = UniFileFactory.fromUFD(cacheFile)

        if (workerFile == null) {
            looger.error("workerFile is null")
            exceptionList.add(ExtensionException("ProviderFactory", "workerFile is null"))
            return Result(providerList, exceptionList)
        }

        if (cacheFile == null) {
            looger.error("cacheFile is null")
            exceptionList.add(ExtensionException("ProviderFactory", "cacheFile is null"))
            return Result(providerList, exceptionList)
        }

        // jsFileProvider
        val jsWorkerFile = workerFile.createDirectory("js")
        if (jsWorkerFile == null) {
            looger.error("jsWorkerFile is null")
            exceptionList.add(ExtensionException("ProviderFactory", "jsWorkerFile is null"))
        } else {
            val jsFileProvider = JSFileExtensionProvider(
                jsWorkerFile,
                scope,
                singleDispatcher,
            )
            providerList.add(jsFileProvider)
        }

        return Result(providerList, exceptionList)
    }

}