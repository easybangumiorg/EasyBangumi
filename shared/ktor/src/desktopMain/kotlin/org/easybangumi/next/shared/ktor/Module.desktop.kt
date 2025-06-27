package org.easybangumi.next.shared.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import org.koin.core.module.Module
import org.koin.dsl.module

actual val ktorModule: Module
    get() = module {
        single {
            HttpClient(Java) {
                install(ContentNegotiation) {
                    json()
                    xml()
                }
                install(HttpCookies) {
                    storage = EasyCookiesStorage()
                }
            }
        }
    }