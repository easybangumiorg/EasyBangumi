package org.easybangumi.next.shared.debug.webview

import org.easybangumi.next.jcef.JcefWebViewProxy
import org.easybangumi.next.lib.webview.IWebView


actual fun getWebView(): IWebView {
    return JcefWebViewProxy()
}

actual suspend fun test2() {
    val jcef = JcefWebViewProxy()
    jcef.loadUrl("https://dm.xifanacg.com/search/wd/%25E9%2587%2591%25E7%2589%258C%25E5%25BE%2597%25E4%25B8%25BB%2520%25E7%25AC%25AC%25E4%25BA%258C%25E5%25AD%25A3/page/1.html")
    jcef.waitingForPageLoaded(5000)
    val content = jcef.getContent(5000)
    logger.info(content)

}