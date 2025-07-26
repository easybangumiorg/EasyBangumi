package org.easybangumi.next.shared.debug.shimmer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.debug.DebugScope
import org.easybangumi.next.shared.foundation.image.AnimationImage
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.shimmer.ShimmerHost
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.scheme.EasyScheme

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
fun DebugScope.ShimmerDebug() {

    var isLoading by remember {
        mutableStateOf(false)
    }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = {
            isLoading = !isLoading
        }) {
            if (isLoading) {
                Text("Stop Shimmer")
            } else {
                Text("Start Shimmer")
            }
        }

        ShimmerHost(
            modifier = Modifier.fillMaxWidth().weight(1f),
            visible = isLoading,
            floatContent = {
                if (it.isVisible) {
                    AnimationImage(
                        modifier = Modifier
                            .rectContentKey("img")
                            .padding(32.dp),
                        model = Res.assets.loading_ryo_gif,
                        contentScale = ContentScale.Crop,
                        contentDescription = "Shimmer Image",
                    )
                }

            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Row {
                    AsyncImage(
                        modifier = Modifier
                            .width(EasyScheme.size.cartoonCoverWidth)
                            .height(EasyScheme.size.cartoonCoverHeight)
                            .mark("img")
                            .drawRectWhenShimmerVisible(),
                        model = Res.images.empty_soyolin,
                        contentScale = ContentScale.Crop,
                        contentDescription = "Shimmer Image",
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Normal", modifier = Modifier)
                        Text(
                            text = "drawRectWhenShimmerVisible and weight 1f",
                            modifier = Modifier.onShimmerVisible {
                                fillMaxWidth()
                            }.drawRectWhenShimmerVisible()
                        )
                        Text("shimmer dismiss", modifier = Modifier.dismissWhenShimmerVisible())
                    }

                }
                Text("Normal", modifier = Modifier)
                Text(
                    text = "drawRectWhenShimmerVisible and fillMaxWidth",
                    modifier = Modifier.onShimmerVisible {
                        fillMaxWidth()
                    }.drawRectWhenShimmerVisible()
                )
                Text("shimmer dismiss", modifier = Modifier.dismissWhenShimmerVisible())
            }
        }



    }

}