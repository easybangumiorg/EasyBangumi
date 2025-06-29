package org.easybangumi.next.shared.ktor

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Created by heyanle on 2025/6/26.
 */

expect val ktorModule: Module


interface KtorConfig {
    fun apply(config: HttpClientConfig<*>)
}

interface KtorFactory {
    fun create(vararg config: KtorConfig): HttpClient
}


object GlobalKtorConfig : KtorConfig {
    override fun apply(config: HttpClientConfig<*>) {
        config.install(ContentNegotiation) {
            json()
            xml()
        }
        config.install(HttpCookies) {
            storage = EasyCookiesStorage()
        }
    }
}