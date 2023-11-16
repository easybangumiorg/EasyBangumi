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
    }

    suspend fun getInfoList(): DataResult<ExtensionStoreRemoteInfo> {
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