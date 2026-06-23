package com.heyanle.easybangumi4.plugin.source.js.source

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Javascript
import com.heyanle.easybangumi4.plugin.source.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.source.js.SourceMetadata
import com.heyanle.easybangumi4.plugin.api.IconSource
import com.heyanle.easybangumi4.plugin.api.Source
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
            importPackage(Packages.com.heyanle.easybangumi4.plugin.source.js.runtime);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.source.js.entity);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.source.js);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.api);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.api.utils.api);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.api.entity);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.source.js.utils);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.api.component.preference);
            
            importPackage(Packages.kotlin.text);
            importPackage(Packages.kotlin);
            
            importPackage(Packages.java.util);
            importPackage(Packages.java.lang);
            importPackage(Packages.java.net);
            
            importPackage(Packages.org.jsoup);
            importPackage(Packages.org.json);
            importPackage(Packages.okhttp3);
            
            importPackage(Packages.javax.crypto);
           
           
            
            var Log = JSLogUtils;
            var SourceUtils = JSSourceUtils;
            var source = Inject_Source;
         
            
            function makeCartoonCover(map) {
                return SourceV3Bridge.makeCartoonCover(Inject_Source.key, map);
            }
            
            function makeCartoon(map) {
                return SourceV3Bridge.makeCartoon(Inject_Source.key, map);
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
        get() = map.get(SourceMetadata.SOURCE_TAG_KEY) ?: ""
    override val label: String
        get() = map.get(SourceMetadata.SOURCE_TAG_LABEL) ?: ""
    override val version: String
        get() = map.get(SourceMetadata.SOURCE_TAG_VERSION_NAME) ?: ""
    override val versionCode: Int
        get() = map.get(SourceMetadata.SOURCE_TAG_VERSION_CODE)?.toIntOrNull() ?: 0



    // 杞婚噺绾ф彃浠剁殑涓氬姟娉ㄥ唽浜ょ粰 JSComponentBundle 澶勭悊
    override fun register(): List<KClass<*>> {
        return emptyList()
    }

    override fun getAsyncIconData(): Any {
        val cover = map.get(SourceMetadata.SOURCE_TAG_COVER) ?: return Icons.Filled.Javascript
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
