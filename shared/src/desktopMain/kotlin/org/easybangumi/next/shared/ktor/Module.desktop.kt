package org.easybangumi.next.shared.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.http.Url
import org.easybangumi.next.shared.preference.NetworkPreference
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module
import java.net.Proxy

actual val ktorModule: Module
    get() = module {
        single {
            DesktopKtorFactory(get())
        }.binds(arrayOf(KtorFactory::class))

        // Global HttpClient
        single {
            get<KtorFactory>().create()
        }.binds(arrayOf(HttpClient::class))
    }


private class DesktopKtorFactory(
    private val networkPreference: NetworkPreference,
) : KtorFactory {

    override fun create(vararg config: KtorConfig): HttpClient {
        return HttpClient(Java) {
            engine {
                when (networkPreference.proxyMode.get()) {
                    NetworkPreference.ProxyMode.DISABLED -> proxy = Proxy.NO_PROXY
                    NetworkPreference.ProxyMode.SYSTEM -> Unit
                    NetworkPreference.ProxyMode.MANUAL -> {
                        val endpoint = parseManualProxyEndpoint(networkPreference.proxyUrl.get())
                        proxy = when {
                            endpoint == null -> Proxy.NO_PROXY
                            networkPreference.proxyProtocol.get() == NetworkPreference.ProxyProtocol.SOCKS5 -> {
                                ProxyBuilder.socks(endpoint.host, endpoint.port)
                            }
                            else -> ProxyBuilder.http(Url(endpoint.toHttpUrl()))
                        }
                    }
                }
            }
            config.forEach {
                it.apply(this)
            }
            install(HttpCookies) {
                storage = JcefCookiesStorage.easyCookiesStorage
            }
            // Global 配置
            GlobalKtorConfig.apply(this)
        }
    }
}

