package com.heyanle.easybangumi4.base.preferences.android

import android.content.SharedPreferences
import com.heyanle.easybangumi4.base.preferences.Preference
import kotlinx.coroutines.flow.Flow

/**
 * Created by HeYanLe on 2023/7/29 16:55.
 * https://github.com/heyanLE
 */
sealed class AndroidPreference<T>(
    private val preferences: SharedPreferences,
    private val keyFlow: Flow<String?>,
    private val key: String,
    private val defaultValue: T,
): Preference<T> {
}