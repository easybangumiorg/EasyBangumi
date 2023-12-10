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
        private const val EXTENSION_STORE_INFO_ROOT_URL = "https://raw.githubusercontent.com/easybangumiorg/EasyBangumi-sources/public/repository/extension"
        private const val EXTENSION_STORE_INFO_URL = "${EXTENSION_STORE_INFO_ROOT_URL}/extension.json"
        const val EXTENSION_STORE_ICON_ROOT_URL = "${EXTENSION_STORE_INFO_ROOT_URL}/icon"
        private const val EXTENSION_STORE_VERSION = 1


    }

    suspend fun getInfoList(): DataResult<ExtensionStoreRemoteInfo> {
        return withContext(Dispatchers.IO) {
            val resp = runCatching {
                OkhttpHelper.client.newCall(
                    Request.Builder().url(EXTENSION_STORE_INFO_URL).get().build()
                ).execute()
            }.getOrElse {
                it.printStackTrace()

                return@withContext  DataResult.error<ExtensionStoreRemoteInfo>(it.message?:"", it)
            }
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