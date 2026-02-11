package org.easybangumi.next.shared.ktor

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import nl.adaptivity.xmlutil.serialization.getPlatformDefaultModule
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
    private var debug = true
    private val logger = org.easybangumi.next.lib.logger.logger("GlobalKtorConfig")
    override fun apply(config: HttpClientConfig<*>) {
        config.install(ContentNegotiation) {
            json()
            xml()
        }
        config.install(HttpCookies) {
            storage = EasyCookiesStorage()
        }
        config.install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
//                    GlobalKtorConfig.logger.info("KtorClient: $message")
                }
            }
            level = if (debug) LogLevel.ALL else LogLevel.INFO
        }

    }
}