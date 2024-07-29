package com.heyanle.easybangumi4.plugin.js.source

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import com.heyanle.easybangumi4.source_api.IconSource
import com.heyanle.easybangumi4.source_api.Source
import okhttp3.HttpUrl
import java.io.File
import java.nio.ByteBuffer

/**
 * Created by heyanle on 2024/7/29.
 * https://github.com/heyanLE
 */
interface AsyncIconSource: Source {


    /**
     * Set the data to load.
     *
     * The default supported data types are:
     * - [String] (mapped to a [Uri])
     * - [Uri] ("android.resource", "content", "file", "http", and "https" schemes only)
     * - [HttpUrl]
     * - [File]
     * - [DrawableRes]
     * - [Drawable]
     * - [Bitmap]
     * - [ByteArray]
     * - [ByteBuffer]
     */
    fun getAsyncIconData(): Any
}


fun IconSource.getIconWithAsyncOrDrawable(): Any? {
    return if(this is AsyncIconSource){
        this.getAsyncIconData()
    }else{
        this.getIconFactory().invoke()
    }
}