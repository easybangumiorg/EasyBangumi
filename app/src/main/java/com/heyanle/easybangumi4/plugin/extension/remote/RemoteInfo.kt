package com.heyanle.easybangumi4.plugin.extension.remote

import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.utils.getMatchReg

/**
 * Created by heyanlin on 2025/8/14.
 */
data class RemoteInfo (
    val key: String = "",
    val url: String = "",
    val label: String = "",
    val versionCode: Int = -1,
    val versionName: String = "",
) {

    fun match(key: String): Boolean {
        var matched = false
        for (match in key.split(',')) {
            val regex = match.getMatchReg()
            if (label.matches(regex)) {
                matched = true
                break
            }
            if (key.matches(regex)) {
                matched = true
                break
            }
            if (label.toString().matches(regex)) {
                matched = true
                break
            }
        }
        return matched
    }
}

data class ExtensionRemoteLocalInfo(
    val remoteInfo: RemoteInfo? = null,
    val localInfo: ExtensionInfo? = null,
) {
    val onlyRemote = remoteInfo != null && localInfo == null
    val onlyLocal = remoteInfo == null && localInfo != null
    val hasUpdate: Boolean = remoteInfo != null && localInfo != null && remoteInfo.versionCode > localInfo.versionCode

    fun match(key: String): Boolean {
        return remoteInfo?.match(key) ?: false || localInfo?.match(key) ?: false
    }
}