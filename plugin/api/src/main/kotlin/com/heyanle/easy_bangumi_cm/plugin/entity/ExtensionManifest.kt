package com.heyanle.easy_bangumi_cm.plugin.entity

/**
 * 拓展清单信息，拓展加载的材料
 * Created by heyanlin on 2024/12/13.
 */
data class ExtensionManifest (
    // 基础信息
    val key: String,
    val status: Int = 0,            // 是否可以加载
    val errorMsg: String? = null,   // 错误信息

    // 元数据
    val label: String = "",
    val readme: String? = null,
    val author: String? = null,
    val icon: Any? = null,
    val versionCode: Long = 0,
    val libVersion: Int = 0,        // 纯纯看番拓展引擎版本

    // 加载数据
    val providerType: Int,          // 提供者类型
    val loadType: Int = 0,          // 加载类型，决定用哪个 Loader 加载


    val sourcePath: String?,        // 拓展源文件位置，可能为空
    val assetsPath: String?,         // 资源文件位置

    val workPath: String,           // 拓展工作路径，对于解压类拓展为解压路径

    val lastModified : Long,        // 最后修改时间，用于判断是否需要重新加载

    // other
    val ext : Any? = null
){

    companion object {
        const val LOAD_TYPE_INNER = 1
        const val LOAD_TYPE_JS_FILE = 2
        const val LOAD_TYPE_JS_PKG = 3

        const val PROVIDER_TYPE_JS_FILE = 1
        const val PROVIDER_TYPE_INNER = 2
        const val PROVIDER_TYPE_PKG = 3

        const val STATUS_CAN_LOAD = 0
        const val STATUS_NEED_REINSTALL = 1
        const val STATUS_LOAD_ERROR = 2

    }

}