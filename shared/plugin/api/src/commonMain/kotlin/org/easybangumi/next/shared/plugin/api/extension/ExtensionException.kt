package org.easybangumi.next.shared.plugin.api.extension

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
class ExtensionException(
    tag: String,
    msg: String
): IllegalStateException("[${tag}]${msg}")

internal fun Any.error(
    msg: String
): Nothing {
    throw ExtensionException(this::class.simpleName ?: this.toString(), msg)
}