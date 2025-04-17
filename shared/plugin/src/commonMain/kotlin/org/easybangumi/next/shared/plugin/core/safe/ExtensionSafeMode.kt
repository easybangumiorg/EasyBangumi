package org.easybangumi.next.shared.plugin.core.safe

import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.plugin.api.component.Component

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
 *
 *
 */
object ExtensionSafeMode {

    private val folder: UniFile? by lazy {
        UniFileFactory.fromUFD(pathProvider.getFilePath("extension"))
    }


    /**
     * 安全模块
     */
    fun isSafeMode(): Boolean {
        return false
    }

    fun onComponentMethodStart(
        component: Component,
        functionName: String,
    ) {


    }

    fun onComponentMethodEnd(
        component: Component,
        functionName: String
    ) {

    }

}