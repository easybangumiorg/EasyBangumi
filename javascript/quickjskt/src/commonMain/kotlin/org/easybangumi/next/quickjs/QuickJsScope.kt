package org.easybangumi.next.quickjs

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.ObjectBinding
import com.dokar.quickjs.binding.define
import kotlinx.coroutines.CoroutineDispatcher

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
class QuickJsScope(
    private val dispatcher: CoroutineDispatcher
) {

    private val quickJs: QuickJs by lazy {
        QuickJs.create(dispatcher)
    }

    fun init() {

        }
    }

    fun newObjectBinding(name: String, binding: ObjectBinding) {

    }

}