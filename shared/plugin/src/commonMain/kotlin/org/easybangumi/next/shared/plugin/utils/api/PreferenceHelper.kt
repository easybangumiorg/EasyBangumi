package org.easybangumi.next.shared.plugin.utils.api

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

interface PreferenceHelper {

    /**
     * 获取所有存储的键值对（无序）
     */
    fun map(): Map<String, String>

    fun get(key: String, def: String): String

    fun put(key: String, value: String)

}