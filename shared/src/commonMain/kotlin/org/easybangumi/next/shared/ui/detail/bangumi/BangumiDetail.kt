package org.easybangumi.next.shared.ui.detail.bangumi

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.easybangumi.ext.shared.plugin.bangumi.plugin.BangumiMetaManager
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.plugin.api.component.ComponentBusiness
import org.easybangumi.next.shared.plugin.api.component.mate.MateComponent

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
@Composable
fun BangumiDetail(
    cartoonIndex: CartoonIndex,
    metaBusiness: ComponentBusiness<MateComponent<BangumiMetaManager>>,
) {

    val vm = vm(
        ::BangumiDetailViewModel, cartoonIndex, metaBusiness
    )

    Column {

    }


}