package com.heyanle.easybangumi4.extension.store

import com.squareup.moshi.Json

/**
 * Created by heyanlin on 2023/11/13.
 */
// 插件市场服务器返回的 json 列表的格式

data class ExtensionStoreInfo(
    val apiVersion: Int,        // 插件市场格式版本
    val infoList: List<ExtensionStoreInfoItem> = emptyList(),
)

data class ExtensionStoreInfoItem(
    @Json(name = "package")
    val pkg: String,            // 包名
    val label: String,          // 名称
    val iconBase64: String,     // 图标 base64
    val versionName: String,    // 版本
    val versionCode: Int,
    val libVersion: Int,        // 对应源 api 的版本
    val fileUrl: String,        // 文件下载地址
    val author: String,         // 作者
    val gitUrl: String,         // 仓库链接
    val releaseDesc: String,    // 仓库 release 描述
    val md5: String,            // 拓展文件 md5
){

    fun getInstalledFileName() = "store-${pkg}.extension.apk"

}





// 插件市场插件 item，本地保存
data class ExtensionStoreItem(
    val extensionStoreInfo: ExtensionStoreInfoItem,
    val state: Int,
) {
    companion object {
        const val STATE_NO_DOWNLOAD = 0         // 未下载
        const val STATE_DOWNLOADING = 1         // 下载中
        const val STATE_DOWNLOADED = 2          // 已下载
        const val STATE_NEED_UPDATE = 4         // 有更新
    }

}