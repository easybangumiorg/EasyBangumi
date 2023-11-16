package com.heyanle.easybangumi4.extension.store

import com.squareup.moshi.Json

/**
 * Created by heyanlin on 2023/11/13.
 */

// 插件市场服务器返回的 json 列表的格式
data class ExtensionStoreRemoteInfo(
    val apiVersion: Int,        // 插件市场格式版本

    // 确保解析没问题，这里加版本需要直接加字段，如果 apiVersion 是 1 就解析 V1
    val extensionsV1: List<ExtensionStoreRemoteInfoItem> = emptyList(),
)

data class ExtensionStoreRemoteInfoItem(
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
data class OfficialExtensionItem(
    val extensionStoreInfo: ExtensionStoreRemoteInfoItem,     // 本地文件对应的云端，做一个储存用于判断是否版本更新
    val realFilePath: String,                           // 在 extension 文件夹里的路径
)


// 插件市场界面展示的 Info，只服务于运行时
data class ExtensionStoreInfo(
    val remote: ExtensionStoreRemoteInfoItem,
    val local: OfficialExtensionItem? = null,   // 可能没下载
    val state: Int,
    val errorMsg: String? = null,
    val throwable: Throwable? = null,
){

    companion object {
        const val STATE_NO_DOWNLOAD = 0
        const val STATE_DOWNLOADING = 1
        const val STATE_ERROR = 2
        const val STATE_INSTALLED = 3
        const val STATE_NEED_UPDATE = 4
    }
}