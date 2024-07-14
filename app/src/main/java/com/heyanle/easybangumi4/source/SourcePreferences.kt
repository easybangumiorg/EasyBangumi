package com.heyanle.easybangumi4.source

import com.heyanle.easybangumi4.base.json.JsonFileProvider
import com.heyanle.easybangumi4.base.preferences.Preference
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson

/**
 * Created by HeYanLe on 2023/7/29 21:34.
 * https://github.com/heyanLE
 */
class SourcePreferences(
    private val preferenceStore: PreferenceStore,
    private val jsonFileProvider: JsonFileProvider,
) {

    val configs = jsonFileProvider.sourceConfig

}