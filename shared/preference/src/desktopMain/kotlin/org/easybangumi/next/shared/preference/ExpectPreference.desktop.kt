package org.easybangumi.next.shared.preference

import org.easybangumi.next.lib.store.preference.PreferenceStore

actual class ExpectPreference actual constructor(preferenceStore: PreferenceStore) {
    val preferenceStore = preferenceStore

    // 按钮1（左键） 按钮1（右键长按） 按钮2（右键） 按钮2（右键长按）
    val fastSeekDuring = preferenceStore.getObject<List<Long>>(
        "fast_seek_during",
        listOf<Long>(
            -15000L, -30000L, 15000L, 30000L
        ),
        serializer = {
                it.joinToString(",") { it.toString() }
        }, deserializer = {
            if (it.isBlank()) {
                emptyList()
            } else {
                it.split(",").mapNotNull { it.toLongOrNull() }
            }
        })

}