package org.easybangumi.next.shared.preference

import org.easybangumi.next.lib.store.preference.PreferenceStore
import org.easybangumi.next.lib.store.preference.getEnum
import org.easybangumi.next.isDesktop
import org.easybangumi.next.platformInformation

class NetworkPreference(
    private val preferenceStore: PreferenceStore,
) {
    enum class ProxyMode {
        DISABLED,
        MANUAL,
        SYSTEM,
    }

    enum class ProxyProtocol {
        HTTP,
        SOCKS5,
    }

    val proxyMode = preferenceStore.getEnum("network_proxy_mode", defaultProxyMode())
    val proxyProtocol = preferenceStore.getEnum("network_proxy_protocol", ProxyProtocol.HTTP)
    val proxyUrl = preferenceStore.getString("network_proxy_url", "")

    private fun defaultProxyMode(): ProxyMode {
        return if (platformInformation.isDesktop()) {
            ProxyMode.SYSTEM
        } else {
            ProxyMode.DISABLED
        }
    }
}
