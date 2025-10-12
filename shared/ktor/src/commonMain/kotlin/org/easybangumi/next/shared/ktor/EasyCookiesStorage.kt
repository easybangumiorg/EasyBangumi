package org.easybangumi.next.shared.ktor

import io.ktor.client.plugins.cookies.ConstantCookiesStorage
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.matches
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.internal.readJson
import org.easybangumi.next.lib.store.file_helper.json.JsonlFileHelper
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.pathProvider

/**
 * Created by heyanle on 2025/6/26.
 */
class EasyCookiesStorage : CookiesStorage {

    companion object {
        private val folder: UFD by lazy {
            pathProvider.getFilePath("ktor")
        }
        private val jsonlFileHelper: JsonlFileHelper<Cookie> by lazy {
            JsonlFileHelper<Cookie>(folder, "cookies", Cookie::class)
        }
    }


    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        var coo = cookie
        if (coo.domain == null) {
            // 如果没有 domain，则使用 requestUrl 的域名作为 domain
            coo = Cookie(
                name = cookie.name,
                value = cookie.value,
                expires = cookie.expires,
                maxAge = cookie.maxAge,
                path = cookie.path,
                secure = cookie.secure,
                httpOnly = cookie.httpOnly,
                extensions = cookie.extensions,
                domain = requestUrl.host,
                encoding = cookie.encoding
            )

        }
        jsonlFileHelper.update {
            (it + coo).filter {
                val data = it.expires
                data == null || data.timestamp > Clock.System.now().toEpochMilliseconds()
            }
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        // 重名只发送最后（新）一个，因为 addCookie 是添加到队尾
        val map = linkedMapOf<String, Cookie>()
        jsonlFileHelper.getSync().forEach {
            runCatching {
                if (it.matches(requestUrl)) {
                    val data = it.expires
                    if (data == null || data.timestamp > Clock.System.now().toEpochMilliseconds()) {
                        map[it.name] = it
                    }
                }
            }
        }
        return map.values.toList()
    }

    override fun close() {

    }
}