package org.easybangumi.next.shared.ui.detail

import androidx.compose.runtime.Composable
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.data.cartoon.CartoonIndex

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
private val logger = logger("DetailPage")
@Composable
fun DetailPage(
    cartoonIndex: CartoonIndex,
) {
    logger.info(cartoonIndex.toString())
//    when (cartoonIndex.source) {
//        BangumiInnerSource.SOURCE_ID -> {
//
//            SourceBundleContainer(
//                modifier = Modifier.fillMaxSize()
//            ) {
//                val bundle = it.componentBundle(BangumiInnerSource.SOURCE_ID)
//                val business = bundle?.getBusiness(MetaComponent::class) as?  ComponentBusiness<BangumiMetaComponent>
//                logger.info(bundle?.getBusiness(MetaComponent::class).toString())
//                if (business != null) {
//                    BangumiDetailPage(cartoonIndex, business)
//                }
//            }
//        }
//    }
}