package org.easybangumi.next.webkit

import android.webkit.WebView

/**
 * Created by heyanlin on 2025/7/18.
 */
fun WebView.clearWeb() {
    clearHistory()
    clearFormData()
    clearMatches()
}

fun WebView.stop() {
    stopLoading()
//    pauseTimers()
}