package org.easybangumi.next

actual val platformInformation: IPlatformInformation
    get() = TODO("Not yet implemented")

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

actual interface IPlatformInformation {
    actual val platformName: String
    actual val isAndroid: Boolean
    actual val isDesktop: Boolean
    actual val isIos: Boolean

}