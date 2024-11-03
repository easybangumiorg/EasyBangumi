package com.heyanle.easybangumi4.plugin.js.source

import android.app.Application
import android.content.Context
import androidx.annotation.WorkerThread
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.plugin.js.component.JSDetailedComponent
import com.heyanle.easybangumi4.plugin.js.component.JSPageComponent
import com.heyanle.easybangumi4.plugin.js.component.JSPlayComponent
import com.heyanle.easybangumi4.plugin.js.component.JSPreferenceComponent
import com.heyanle.easybangumi4.plugin.js.component.JSSearchComponent
import com.heyanle.easybangumi4.plugin.source.bundle.ComponentBundle
import com.heyanle.easybangumi4.plugin.source.bundle.ComponentProxy
import com.heyanle.easybangumi4.source_api.component.Component
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.play.PlayComponent
import com.heyanle.easybangumi4.source_api.component.preference.PreferenceComponent
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.utils.api.CaptchaHelper
import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.source_api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.source_api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.source_api.utils.api.StringHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

/**
 * Created by heyanle on 2024/7/28.
 * https://github.com/heyanLE
 */
class JSComponentBundle(
    private val jsSource: JsSource
): ComponentBundle {

    private val bundle: HashMap<KClass<*>, Any> = hashMapOf()
    private val componentProxy:  HashMap<KClass<*>, Any> = hashMapOf()

    @WorkerThread
    override suspend fun init() {
        // 1. 注入工具类
        put(StringHelper::class, Inject.get(jsSource.key))
        put(NetworkHelper::class, Inject.get(jsSource.key))
        put(OkhttpHelper::class, Inject.get(jsSource.key))
        put(PreferenceHelper::class, Inject.get(jsSource.key))
        put(WebViewHelper::class, Inject.get(jsSource.key))
        put(CaptchaHelper::class, Inject.get(jsSource.key))
        put(WebViewHelperV2::class, Inject.get(jsSource.key))

        put(Context::class, APP)
        put(Application::class, APP)

        ComponentBundle.sourceClazz.forEach {
            if(it.isInstance(jsSource)){
                putAnyway(it, jsSource)
            }
        }

        val jsFile = jsSource.getJsFile()


        jsSource.jsScope.runWithScope { context, scriptable ->
            // 2. import
            context.evaluateString(
                scriptable,
                JsSource.JS_IMPORT,
                "import",
                1,
                null
            )

            // 3. 加载插件源代码
            if (jsFile == null) {
                context.evaluateString(
                    scriptable,
                    jsSource.getJsString(),
                    "source ${jsSource.key}",
                    1,
                    null
                )
            } else {
                val reader = jsFile.reader()
                context.evaluateReader(
                    scriptable,
                    reader,
                    "source ${jsSource.key}",
                    1,
                    null
                )
            }



            // 4. 注入工具给 JS
            bundle.forEach { (k, v) ->
                k.simpleName.logi("JsImport")
                scriptable.put(k.simpleName, scriptable, v)
            }
        }

        // 5. 检查 & 加载 Component
        val jsSearchComponent = JSSearchComponent.of(jsSource.jsScope)
        val jsPageComponent = JSPageComponent.of(jsSource.jsScope)
        val jsPlayComponent = JSPlayComponent.of(jsSource.jsScope)
        val jsDetailedComponent = JSDetailedComponent.of(jsSource.jsScope)
        val jsPreferenceComponent = JSPreferenceComponent.of(jsSource.jsScope)

        if(jsSearchComponent != null){
            jsSearchComponent.innerSource = jsSource
            jsSearchComponent.init()
            put(SearchComponent::class, jsSearchComponent)
        }
        if(jsPageComponent != null){
            jsPageComponent.innerSource = jsSource
            jsPageComponent.init()
            put(PageComponent::class, jsPageComponent)
        }
        if(jsPlayComponent != null){
            jsPlayComponent.innerSource = jsSource
            jsPlayComponent.init()
            put(PlayComponent::class, jsPlayComponent)
        }
        if(jsDetailedComponent != null){
            jsDetailedComponent.innerSource = jsSource
            jsDetailedComponent.init()
            put(DetailedComponent::class, jsDetailedComponent)
        }
        if(jsPreferenceComponent != null){
            jsPreferenceComponent.innerSource = jsSource
            jsPreferenceComponent.init()
            put(PreferenceComponent::class, jsPreferenceComponent)
        }


    }

    override fun get(clazz: KClass<*>): Any? {
        return bundle[clazz]
    }

    override fun getComponentProxy(clazz: KClass<*>): Any? {
        val o = componentProxy[clazz]
        if (o == null){
            val instance = get(clazz) ?: return null
            if (!clazz.isInstance(instance) || instance !is Component){
                return null
            }
            val proxy = Proxy.newProxyInstance(instance.javaClass.classLoader, instance.javaClass.interfaces, ComponentProxy(instance))
            componentProxy[clazz] = proxy
            return proxy
        }
        return o
    }

    override fun release() {
        bundle.clear()
    }

    private fun putAnyway(clazz: KClass<*>, instance: Any){
        bundle[clazz] = instance
    }

    private fun <T : Any> put(clazz: KClass<T>, instance: T) {
        bundle[clazz] = instance
    }

}