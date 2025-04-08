package org.easybangumi.next.lib.global

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
object CoroutineProvider

expect fun CoroutineProvider.io(): Dispatchers

expect fun CoroutineProvider.main(): Dispatchers

expect fun CoroutineProvider.single(): Dispatchers

expect fun CoroutineProvider.newSinge(name: String): Dispatchers