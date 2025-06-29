package org.easybangumi.next

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

enum class PlatformType {
    Android,
    Ios,
    Desktop
}

expect interface Platform {

    val platformType: PlatformType
    val platformName: String

    val isDebug: Boolean

    val versionCode: Int
    val versionName: String


}

fun Platform.isAndroid(): Boolean = platformType == PlatformType.Android
fun Platform.isIos(): Boolean = platformType == PlatformType.Ios
fun Platform.isDesktop(): Boolean = platformType == PlatformType.Desktop

val platformInformation: Platform
    get() = KoinPlatform.getKoin().get<Platform>()


