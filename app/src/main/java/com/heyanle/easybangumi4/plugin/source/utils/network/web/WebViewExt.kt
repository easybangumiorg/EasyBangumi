package com.heyanle.easybangumi4.plugin.source.utils.network.web

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
    pauseTimers()
}