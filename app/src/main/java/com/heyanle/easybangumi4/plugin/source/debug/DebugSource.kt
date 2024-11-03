package com.heyanle.easybangumi4.plugin.source.debug

import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.story.local.source.LocalSourceComponent
import com.heyanle.extension_api.ExtensionIconSource
import kotlin.reflect.KClass


/**
 * Created by HeYanLe on 2024/11/3 19:52.
 * https://github.com/heyanLE
 */

object DebugSource: ExtensionIconSource {

    const val DEBUG_SOURCE_KEY = "easybangumi_debug"

    override fun getIconResourcesId(): Int? = null
    override fun getIconFactory(): () -> Drawable? {
        return {
            ResourcesCompat.getDrawable(APP.resources, R.mipmap.logo_new, null)
        }
    }

    override val describe: String?
        get() = "调试番剧"
    override val key: String
        get() = DEBUG_SOURCE_KEY
    override val label: String
        get() = "调试番源"
    override val version: String
        get() = "1.0.0"
    override val versionCode: Int
        get() = 0

    override fun register(): List<KClass<*>> {
        return listOf(
            DebugSourceComponent::class
        )
    }

}