package org.easybangumi.next.lib.unifile

import kotlinx.serialization.Serializable
import okio.Path.Companion.toPath

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
@Serializable
data class UFD(
    val type: String,
    // 语义随着 type 不同有不同的诠释
    val uri: String,

    // 预设，先预埋，可能后续会有跨平台交互路径的需求，可通过预设在特定平台自己获取最终路径
    val preset: String = "",
){
    companion object {
        const val TYPE_OKIO = "okio"
        const val TYPE_JVM = "jvm"
        const val TYPE_ANDROID_UNI = "android_uni"
        const val TYPE_ASSETS = "assets"
    }

    fun child(display: String): UFD? {
        when (type) {
            TYPE_OKIO -> {
                val child = uri.toPath().div(display)
                return UFD(TYPE_OKIO, child.toString())
            }
            TYPE_JVM -> {
                val child = uri.toPath().div(display)
                return UFD(TYPE_JVM, child.toString())
            }
            TYPE_ASSETS -> {
                val child = uri.toPath().div(display)
                return UFD(TYPE_ASSETS, child.toString())
            }
            else -> {
                val uni = UniFileFactory.fromUFD(this) ?: return null
                return uni.child(display)?.getUFD()
            }
        }
    }

}