package com.heyanle.easy_bangumi_cm.base.path_provider

/**
 * Created by heyanlin on 2024/11/27.
 */
data class PathInfo(
    val type: Int,
    val uri: String,
) {

    companion object {
        const val TYPE_CONTENT_URI = 0
        const val TYPE_ABSOLUTE_PATH = 1
    }

    fun isContentUrl(): Boolean {
        return type == TYPE_CONTENT_URI
    }

    fun isAbsolutePath(): Boolean {
        return type == TYPE_ABSOLUTE_PATH
    }


}