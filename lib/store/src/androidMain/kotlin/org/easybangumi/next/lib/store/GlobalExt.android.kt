package org.easybangumi.next.lib.store

import org.easybangumi.next.lib.global.Global
import org.easybangumi.next.lib.global.getAppContext
import org.easybangumi.next.lib.store.preference.AndroidPreferenceStore
import org.easybangumi.next.lib.store.preference.PreferenceStore

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

private val androidPreferenceStore: PreferenceStore by lazy {
    AndroidPreferenceStore(Global.getAppContext())
}

actual fun Global.preferenceStore(): PreferenceStore {
    return androidPreferenceStore
}