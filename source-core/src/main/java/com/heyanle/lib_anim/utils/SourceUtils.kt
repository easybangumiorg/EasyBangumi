package com.heyanle.lib_anim.utils

object SourceUtils {
    fun urlParser(rootURL:String,source: String): String {
        return when {
            source.startsWith("http") -> {
                source
            }

            source.startsWith("//") -> {
                "https:$source"
            }

            source.startsWith("/") -> {
                rootURL + source
            }

            else -> {
                "${rootURL}/$source"
            }
        }
    }
}
