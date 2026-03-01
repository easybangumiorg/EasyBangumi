package org.easybangumi.next.shared.compose.browser

import androidx.compose.runtime.mutableStateOf
import org.easybangumi.next.shared.foundation.view_model.StateViewModel

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
abstract class BrowserPageVM(
    protected val param: BrowserPageParam,
) : StateViewModel<BrowserPageVM.State>(State.Initializing) {

    sealed class State {
        object Initializing : State()
        object Loading : State()
        data class BrowserReady(val canGoBack: Boolean, val canGoForward: Boolean) : State()
        data class Error(val errorMsg: String) : State()
    }

    // 浏览器状态
    val currentUrl = mutableStateOf(param.url)
    val urlInput = mutableStateOf(param.url)
    val isLoading = mutableStateOf(false)
    val canGoBack = mutableStateOf(false)
    val canGoForward = mutableStateOf(false)
    val pageTitle = mutableStateOf(param.title ?: "Browser")

    // 浏览器控制接口
    abstract fun goBack()
    abstract fun goForward()
    abstract fun reload()
    abstract fun stopLoading()
    abstract fun loadUrl(url: String)

}