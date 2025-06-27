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
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.pathProvider

/**
 * Created by heyanlin on 2025/6/26.
 */
class EasyCookiesStorage : CookiesStorage {

    private val folder = pathProvider.getFilePath("ktor")
    private val jsonlFileHelper = JsonlFileHelper<Cookie>(folder, "cookies.jsonl", Cookie::class)

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        jsonlFileHelper.update {
            (it + cookie).filter {
                val data = it.expires
                data == null || data.timestamp > Clock.System.now().toEpochMilliseconds()
            }
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        return jsonlFileHelper.getSync().filter {
            val data = it.expires
            it.matches(requestUrl) && (data == null || data.timestamp > Clock.System.now().toEpochMilliseconds())
        }
    }

    override fun close() {

    }
}