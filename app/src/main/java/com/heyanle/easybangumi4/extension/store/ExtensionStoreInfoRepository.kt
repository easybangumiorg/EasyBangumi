package com.heyanle.easybangumi4.extension.store

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.utils.OkhttpHelper
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

/**
 * Created by heyanlin on 2023/11/13.
 */
class ExtensionStoreInfoRepository {

    companion object {
        private const val EXTENSION_STORE_INFO_URL = ""
        private const val EXTENSION_STORE_VERSION = 1

        private const val DEBUG = true
    }

    suspend fun getInfoList(): DataResult<ExtensionStoreRemoteInfo> {
        if (DEBUG){
            return DataResult.ok(ExtensionStoreRemoteInfo(1, listOf(
                ExtensionStoreRemoteInfoItem(
                    pkg = "com.heyanle.easybangumi_extension.animone",
                    label = "Animone",
                    iconUrl = "https://raw.githubusercontent.com/easybangumiorg/EasyBangumi-sources/main/icon/anim1.png",
                    versionCode = 1,
                    versionName = "1.0",
                    libVersion = 4,
                    author = "heyanle",
                    gitUrl = "https://github.com/heyanLE/EasyBangumi-Extension-animeone",
                    releaseDesc = "test",
                    md5 = "d68d74244e597394d0427617cc97d22f",
                    fileSize = -1,
                    fileUrl = "https://github.com/heyanLE/EasyBangumi-Extension-animeone/releases/download/1.0/extension.apk"
                )
            )))
        }

        return withContext(Dispatchers.IO) {
            val resp = OkhttpHelper.client.newCall(
                Request.Builder().url(EXTENSION_STORE_INFO_URL).get().build()
            ).execute()
            val body = resp.body?.string()
            if (!resp.isSuccessful || body == null) {
                return@withContext DataResult.error<ExtensionStoreRemoteInfo>(resp.message)
            }
            val info = body.jsonTo<ExtensionStoreRemoteInfo>()
            if(info == null || info.apiVersion > EXTENSION_STORE_VERSION){
                return@withContext DataResult.error<ExtensionStoreRemoteInfo>(stringRes(com.heyanle.easy_i18n.R.string.easybangumi_too_old))
            }
            return@withContext DataResult.ok(info)
        }
    }

}