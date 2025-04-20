package org.easybangumi.next.shared.plugin.utils.core

import org.easybangumi.next.lib.store.JournalMapHelper
import org.easybangumi.next.shared.plugin.api.source.Source
import org.easybangumi.next.shared.plugin.api.utils.PreferenceHelper

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
        source.scope
    )

    override fun map(): Map<String, String> {
        return jm.mapSync()
    }

    override fun get(key: String, def: String): String {
        return jm.getSync(key, def)
    }

    override fun put(key: String, value: String) {
        jm.put(key, value)
    }
}