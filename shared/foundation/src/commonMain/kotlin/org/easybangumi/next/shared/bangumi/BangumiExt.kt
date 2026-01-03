package org.easybangumi.next.shared.bangumi

import androidx.compose.ui.graphics.Color
import org.easybangumi.next.shared.bangumi._bangumiColor
import org.easybangumi.next.shared.data.bangumi.BangumiConst

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
private var _bangumiColor = Color(0xFFE4999E)
fun BangumiConst.bangumiColor(): Color {
    return _bangumiColor
}