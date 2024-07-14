package com.heyanle.easybangumi4.base.json


import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.source.SourceConfig
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.getFilePath
import com.hippo.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.File
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

/**
 * Created by heyanle on 2024/7/14.
 * https://github.com/heyanLE
 */
class JsonFileProvider {

    private val dispatcher = CoroutineProvider.CUSTOM_SINGLE
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // /storage/emulated/0/Android/data/com.heyanle.easybangumi4/files/cartoon/cartoon_tag.json
    val cartoonTag: JsonFileHelper<List<CartoonTag>> = JsonFileHelper(
        folder = UniFile.fromFile(File(APP.getFilePath("cartoon")))!!,
        name = "cartoon_tag.json",
        def = emptyList(),
        scope = scope,
        type = typeOf<List<CartoonTag>>().javaType
    )

    // /storage/emulated/0/Android/data/com.heyanle.easybangumi4/files/source/source_config.json
    val sourceConfig: JsonFileHelper<Map<String, SourceConfig>> = JsonFileHelper(
        folder = UniFile.fromFile(File(APP.getFilePath("source")))!!,
        name = "source_config.json",
        def = emptyMap(),
        scope = scope,
        type = typeOf<Map<String, SourceConfig>>().javaType
    )

}