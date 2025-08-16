package com.heyanle.easybangumi4.plugin.extension.remote

import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo

/**
 * Created by heyanlin on 2025/8/14.
 */
data class RemoteInfo (
    val key: String = "",
    val icon: String = "",
    val url: String = "",
    val label: String = "",
    val versionCode: Int = -1,
    val versionName: String = "",
)

data class ExtensionRemoteLocalInfo(
    val remoteInfo: RemoteInfo? = null,
    val localInfo: ExtensionInfo? = null,
) {
    val onlyRemote = remoteInfo != null && localInfo == null
    val onlyLocal = remoteInfo == null && localInfo != null
    val hasUpdate: Boolean = remoteInfo != null && localInfo != null && remoteInfo.versionCode > localInfo.versionCode
}