package com.heyanle.easybangumi4.plugin.source.json

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import com.heyanle.easybangumi4.plugin.api.IconSource
import com.heyanle.easybangumi4.plugin.api.Source
import com.heyanle.easybangumi4.plugin.source.jsengine.source.AsyncIconSource
import java.io.File
import kotlin.reflect.KClass

class JsonSource(
    val rule: JsonSourceRule,
    val file: File,
) : Source, AsyncIconSource, IconSource {

    override val key: String = rule.key
    override val label: String = rule.label
    override val version: String = rule.versionName
    override val versionCode: Int = rule.versionCode
    override val describe: String? = rule.describe

    override fun register(): List<KClass<*>> = emptyList()

    override fun getAsyncIconData(): Any {
        val cover = rule.cover ?: return Icons.Filled.DataObject
        val uri = Uri.parse(cover)
        uri.scheme?.let {
            if (it == "http" || it == "https" || it == "content" || it == "file") {
                return uri
            }
        }
        return runCatching {
            Base64.decode(cover, Base64.DEFAULT)
        }.getOrDefault(Icons.Filled.DataObject)
    }

    override fun getIconFactory(): () -> Drawable? = { null }
}
