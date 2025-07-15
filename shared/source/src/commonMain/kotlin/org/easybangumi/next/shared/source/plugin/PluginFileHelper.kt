package org.easybangumi.next.shared.source.plugin

import okio.BufferedSource

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
object PluginFileHelper {

    fun getManifestFromNormal(source: BufferedSource) : Map<String, String> {
        try {

            val map = mutableMapOf<String, String>()
            while (true) {
                val line = source.readUtf8Line() ?: break
                if (!line.startsWith("//")) {
                    break
                }
                val atIndex = line.indexOf("@")
                val spaceIndex = line.indexOf(" ")
                if (atIndex == -1 || spaceIndex == -1) {
                    continue
                }
                val key = line.substring(2, atIndex)
                val value = line.substring(atIndex + 1, spaceIndex)
                map[key] = value
            }
            return map
        } catch (e: Exception) {
            return emptyMap()
        }
    }


}