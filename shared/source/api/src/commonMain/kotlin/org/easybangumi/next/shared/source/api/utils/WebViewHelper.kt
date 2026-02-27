package org.easybangumi.next.shared.source.api.utils

import org.easybangumi.next.lib.webview.IWebView
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * Created by HeYanLe on 2024/12/8 23:03.
 * https://github.com/heyanLE
 */

interface WebViewHelper {

    suspend fun <R> use(block: suspend IWebView.()->R): R?

    // 记得关闭
    suspend fun newWebView(): IWebView

}

public inline fun <R> IWebView.useWithHook(block: (IWebView) -> R): R {
//    contract {
//        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
//    }
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.closeFinally(exception)
    }
}

fun AutoCloseable?.closeFinally(cause: Throwable? = null): Unit = when {
    this == null -> {}
    cause == null -> close()
    else ->
        try {
            close()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
}