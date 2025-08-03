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
            importPackage(Packages.com.heyanle.easybangumi4.source_api);
            importPackage(Packages.com.heyanle.easybangumi4.source_api.utils.api);
            importPackage(Packages.com.heyanle.easybangumi4.source_api.entity);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.js.utils);
            importPackage(Packages.com.heyanle.easybangumi4.source_api.component.preference);
            
            importPackage(Packages.kotlin.text);
            importPackage(Packages.kotlin);
            
            importPackage(Packages.java.util);
            importPackage(Packages.java.lang);
            importPackage(Packages.java.net);
            
            importPackage(Packages.org.jsoup);
            importPackage(Packages.org.json);
            importPackage(Packages.okhttp3);
           
           
            
            var Log = JSLogUtils;
            var SourceUtils = JSSourceUtils;
            var source = Inject_Source;
         
            
            function makeCartoonCover(map) {
                var id = map.id;
                var source = Inject_Source.key;
                var url = map.url;
                var title = map.title;
                var intro = map.intro;
                var cover = map.cover;
                return new CartoonCoverImpl(id, source, url, title, intro, cover);
            }
            
            function makeCartoon(map) {
                var id = map.id;
                var source = Inject_Source.key;
                var url = map.url;
                
                var title = map.title;
                
                
                var genre = null;
                var coverUrl = null;
                var intro = null;
                var description = null;
                var updateStrategy = 0;
                var isUpdate = false;
                var status = 0;
                
                if (map.genreList != undefined) {
                    var stringBuilder = new StringBuilder();
                    for (var i = 0 ; i < map.genreList.length; i++) {
                        stringBuilder.append(map.genreList[i]);
                        if(i != map.genreList.length - 1) {
                            stringBuilder.append(", ");
                        }
                    }
                    genre = stringBuilder.toString();
                }
                
                if (map.genre != undefined) {
                    genre = map.genre;
                }
                
              
                if (map.cover != undefined) {
                    coverUrl = map.cover;
                }
                
                if (map.intro != undefined) {
                    intro = map.intro;
                }
                
               
                if (map.description != undefined) {
                    description = map.description;
                }
                
                if (map.updateStrategy != undefined) {
                    updateStrategy = map.updateStrategy;
                }
                
                if (map.isUpdate != undefined) {
                    isUpdate = map.isUpdate;
                }
                
                if (map.status != undefined) {
                    status = map.status;
                }
                
                return new CartoonImpl(
                    id, source, url, title, genre, coverUrl, intro, description, updateStrategy, isUpdate, status
                );
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