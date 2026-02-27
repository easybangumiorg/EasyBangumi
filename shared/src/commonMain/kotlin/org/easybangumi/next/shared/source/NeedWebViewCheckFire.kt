package org.easybangumi.next.shared.source

import androidx.navigation.NavHostController
import org.easybangumi.next.shared.compose.web.WebPageParam
import org.easybangumi.next.shared.source.api.component.NeedWebViewCheckException


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

fun NeedWebViewCheckException.fire(
    navController: NavHostController,
    onDismiss: () -> Unit = {},
) {
    val webPageParam = WebPageParam(
        title = "网页效验",
        url = this.url,
    )



}