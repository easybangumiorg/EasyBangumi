package com.heyanle.easybangumi4.plugin.js.source

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Javascript
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionLoader
import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.source_api.IconSource
import com.heyanle.easybangumi4.source_api.Source
import java.io.File
import kotlin.reflect.KClass

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class JsSource(
    val map: Map<String, String>,
    val js: Any,
    val jsScope: JSScope,
): Source, AsyncIconSource, IconSource {

    companion object {
        const val JS_IMPORT = """
            importPackage(Packages.com.heyanle.easybangumi4.plugin.extension);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.js.runtime);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.js.entity);
            importPackage(Packages.org.jsoup);
            importPackage(Packages.okhttp3);
            importPackage(Packages.com.heyanle.easybangumi4.source_api.utils.api);
            importPackage(Packages.com.heyanle.easybangumi4.source_api.entity);
            importPackage(Packages.kotlin.text);
            importPackage(Packages.kotlin);
            importPackage(Packages.java.util);
            importPackage(Packages.java.lang);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.js.utils);
            
            var Log = com.android.util.Log;
         
            
            function makeCartoonCover(map) {
                var id = map.id;
                var source = Source.key;
                var url = map.url;
                var title = map.title;
                var intro = map.intro;
                var cover = map.cover;
                return new CartoonCoverImpl(id, source, url, title, intro, cover);
            }
        """
    }


    fun getJsString(): String {
        return if (js is File) {
            js.readText()
        } else {
            js.toString()
        }
    }

    fun getJsFile(): File? {
        return js as? File
    }


    override val describe: String?
        get() = map.get("describe")
    override val key: String
        get() = map.get(JSExtensionLoader.JS_SOURCE_TAG_KEY) ?: ""
    override val label: String
        get() = map.get(JSExtensionLoader.JS_SOURCE_TAG_LABEL) ?: ""
    override val version: String
        get() = map.get(JSExtensionLoader.JS_SOURCE_TAG_VERSION_NAME) ?: ""
    override val versionCode: Int
        get() = map.get(JSExtensionLoader.JS_SOURCE_TAG_VERSION_CODE)?.toIntOrNull() ?: 0



    // 轻量级插件的业务注册交给 JSComponentBundle 处理
    override fun register(): List<KClass<*>> {
        return emptyList()
    }

    override fun getAsyncIconData(): Any {
        val cover = map.get(JSExtensionLoader.JS_SOURCE_TAG_COVER) ?: return Icons.Filled.Javascript
        // url
        val uri = Uri.parse(cover)
        uri.scheme?.let {
            if(it == "http" || it == "https" || it == "content" || it == "file"){
                return uri
            }
        }
        // base64
        try {
            return Base64.decode(cover, Base64.DEFAULT)
        }catch (e: Throwable) {
            e.printStackTrace()
        }

        return Icons.Filled.Javascript
    }

    override fun getIconFactory(): () -> Drawable? {
        return { null }
    }


}