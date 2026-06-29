package org.easybangumi.next.shared.foundation.lazy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion
import androidx.compose.ui.Modifier

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

fun LazyListScope.itemsFromGrid(
    itemsCount: Int,
    girdCount: Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    rowModifier: Modifier = Modifier,
    key: ((firstIndex: Int) -> Any)? = null,
    contentType: (firstIndex: Int) -> Any? = { null },
    itemContent: @Composable BoxScope.(index: Int) -> Unit
){
    val lineCount = itemsCount/girdCount + if (itemsCount%girdCount == 0) 0 else 1
    items(
        lineCount,
        key = if (key == null) key else { { key.invoke(it * girdCount)}},
        contentType = {contentType.invoke(it * girdCount)}
    ) {
        val firstIndex = it * girdCount
        val endIndex = (firstIndex + girdCount - 1)
        Row (
            modifier = rowModifier,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
        ) {
            for (i in firstIndex..endIndex) {
                if (i < itemsCount) {
                    Box(modifier = Modifier.weight(1f)) {
                        itemContent(i)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

            }
        }
    }
}