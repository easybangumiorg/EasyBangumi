package com.heyanle.easybangumi4.utils.exo_ssl

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class TrustAllHostnameVerifier : HostnameVerifier {
    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        return true
    }
}