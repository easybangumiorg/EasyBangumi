package org.easybangumi.next.shared.source

import androidx.navigation.NavHostController
import org.easybangumi.next.lib.utils.WeakRef
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.compose.web.WebPageParam
import org.easybangumi.next.shared.compose.web.needWebViewCheckParamMap
import org.easybangumi.next.shared.source.api.component.NeedWebViewCheckException
import org.easybangumi.next.shared.source.api.component.WebViewCheckParam
import kotlin.time.Clock


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

fun WebViewCheckParam.fire(
    navController: NavHostController,
) {
    val key = Clock.System.now().toString()
    needWebViewCheckParamMap[key] = WeakRef(this)
    val page = WebPageParam(
        key,
    )
    val webPage = RouterPage.WebPage(page)
    navController.navigate(webPage)

}