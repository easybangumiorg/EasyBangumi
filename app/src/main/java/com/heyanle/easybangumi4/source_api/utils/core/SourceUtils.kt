package com.heyanle.easybangumi4.source_api.utils.core

object SourceUtils {
    fun urlParser(rootURL: String, source: String): String {
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
