package org.easybangumi.next.rhino

import kotlinx.coroutines.CoroutineDispatcher
import org.slf4j.Logger
import java.util.ServiceLoader

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
interface RhinoService {

    companion object {

        @Volatile
        internal var directService : RhinoService? = null

        fun initDirectService(service: RhinoService) {
            if (directService != null) {
                throw IllegalStateException("RhinoService is already initialized.")
            }
            directService = service
        }

        val service by lazy {
            directService ?:
            ServiceLoader.load(RhinoService::class.java).firstOrNull() ?: throw IllegalStateException(
                "No RhinoService implementation found. Please ensure you have a valid implementation in your classpath."
            )
        }

        internal fun logger(tag: String): Lazy<Logger> = lazy {
            service.getLogger(tag)
        }
    }

    fun getSingletonDispatcher(): CoroutineDispatcher

    // 使用 slf4j Logger
    fun getLogger(tag: String): Logger



}

