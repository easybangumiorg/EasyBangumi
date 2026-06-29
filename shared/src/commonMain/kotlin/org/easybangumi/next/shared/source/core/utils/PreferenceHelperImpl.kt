package org.easybangumi.next.shared.source.core.utils

import org.easybangumi.next.lib.store.JournalMapHelper
import org.easybangumi.next.shared.source.api.source.Source
import org.easybangumi.next.shared.source.api.utils.PreferenceHelper

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

class PreferenceHelperImpl(
    private val source: Source
): PreferenceHelper {

    private val jm = JournalMapHelper(
        source.workPath,
        "preference",
    )

    override suspend fun map(): Map<String, String> {
        return jm.mapSync()
    }

    override suspend fun get(key: String, def: String): String {
        if (!jm.isSet(key)) {
            return def
        }
        return jm.get(key).ifEmpty { def }
    }

    override suspend fun put(key: String, value: String) {
        jm.putAndWait(key, value)
    }
}