package org.easybangumi.next.shared.ktor

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

actual val ktorModule: Module
    get() = module {
        single {
            DesktopKtorFactory()
        }.binds(arrayOf(KtorFactory::class))

        // Global HttpClient
        single {
            get<KtorFactory>().create()
        }.binds(arrayOf(HttpClient::class))
    }


private class DesktopKtorFactory : KtorFactory {

    override fun create(vararg config: KtorConfig): HttpClient {
        return HttpClient(Java) {
            config.forEach {
                it.apply(this)
            }
            install(HttpCookies) {
                storage = JcefCookiesStorage()
            }
            // Global 配置
            GlobalKtorConfig.apply(this)
        }
    }
}

