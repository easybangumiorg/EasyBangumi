package org.easybangumi.next.shared.ktor

import io.ktor.client.*
import io.ktor.client.engine.android.*
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

actual val ktorModule: Module
    get() = module {
        single {
            AndroidKtorFactory()
        }.binds(arrayOf(KtorFactory::class))

        // Global HttpClient
        single {
            get<KtorFactory>().create()
        }.binds(arrayOf(HttpClient::class))
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
