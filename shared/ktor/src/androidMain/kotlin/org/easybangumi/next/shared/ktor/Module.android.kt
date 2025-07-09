package org.easybangumi.next.shared.ktor

import android.webkit.CookieManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

actual val ktorModule: Module
    get() = module {
        single {
            AndroidKtorFactory()
        }.binds(arrayOf(KtorFactory::class))
    }


private class AndroidKtorFactory : KtorFactory {

    override fun create(vararg config: KtorConfig): HttpClient {
        return HttpClient(Android) {
            config.forEach {
                it.apply(this)
            }
            // Global 配置
            GlobalKtorConfig.apply(this)

        }
    }
}
