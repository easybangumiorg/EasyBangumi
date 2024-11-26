package com.heyanle.easybangumi4.plugin.source.bundle

import android.app.Application
import android.content.Context
import com.heyanle.easybangumi4.source_api.IconSource
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.component.Component
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.play.PlayComponent
import com.heyanle.easybangumi4.source_api.component.preference.PreferenceComponent
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.component.update.UpdateComponent
import com.heyanle.easybangumi4.source_api.utils.api.CaptchaHelper
import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.source_api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.source_api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.source_api.utils.api.StringHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.extension_api.ExtensionIconSource
import com.heyanle.extension_api.ExtensionSource
import kotlin.reflect.KClass

/**
 * Created by heyanle on 2024/7/28.
 * https://github.com/heyanLE
 */
interface ComponentBundle {

    companion object {
        // 以下为 Bundle 需要处理的接口

        // Android 上下文相关
        val contextClazz: Set<KClass<*>> = setOf(
            Context::class,
            Application::class,
        )

        // Source 接口
        val sourceClazz: Set<KClass<*>> = setOf(
            ExtensionSource::class,
            IconSource::class,
            ExtensionIconSource::class,
            Source::class,

            )

        // 工具类接口
        val utilsClazz: Set<KClass<*>> = setOf(
            StringHelper::class,
            NetworkHelper::class,
            OkhttpHelper::class,
            PreferenceHelper::class,
            WebViewHelper::class,
            WebViewHelperV2::class,
            CaptchaHelper::class
        )

        // Component 接口
        val componentClazz: Set<KClass<*>> = setOf(
            PageComponent::class,
            DetailedComponent::class,
            SearchComponent::class,
            PreferenceComponent::class,
            UpdateComponent::class,
            PlayComponent::class
        )
    }

    suspend fun init()

    fun get(clazz: KClass<*>): Any?

    fun getComponentProxy(clazz: KClass<*>): Any?

    fun release()
}

inline fun <reified T: Component> ComponentBundle.getComponentProxy(): T? {
    val obj = getComponentProxy(T::class)
    return obj as? T
}

inline fun <reified T> ComponentBundle.get(): T? {
    return get(T::class) as? T
}