package org.easybangumi.next.shared.ktor

import org.easybangumi.next.shared.preference.NetworkPreference

data class ManualProxyEndpoint(
    val host: String,
    val port: Int,
) {
    fun toHttpUrl(): String = "http://$host:$port"
}

fun parseManualProxyEndpoint(raw: String): ManualProxyEndpoint? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) {
        return null
    }

    val withoutScheme = trimmed.substringAfter("://", trimmed)
    val authority = withoutScheme
        .substringBefore("/")
        .substringBefore("?")
        .substringBefore("#")
        .substringBefore("@")
        .trim()

    val portSeparator = authority.lastIndexOf(':')
    if (portSeparator <= 0 || portSeparator == authority.lastIndex) {
        return null
    }

    val host = authority.substring(0, portSeparator).trim().trim('[', ']')
    val port = authority.substring(portSeparator + 1).trim().toIntOrNull()
    if (host.isEmpty() || port == null || port !in 1..65535) {
        return null
    }
    return ManualProxyEndpoint(host, port)
}

fun NetworkPreference.ProxyMode.supportedOnAndroid(): NetworkPreference.ProxyMode {
    return if (this == NetworkPreference.ProxyMode.SYSTEM) {
        NetworkPreference.ProxyMode.DISABLED
    } else {
        this
    }
}
