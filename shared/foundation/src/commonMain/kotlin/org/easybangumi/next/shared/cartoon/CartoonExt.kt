package org.easybangumi.next.shared.cartoon

import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.data.cartoon.CartoonTag
import org.easybangumi.next.shared.resources.Res

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
fun CartoonTag.displayName(): ResourceOr {
    return when (label) {
        CartoonTag.Companion.DEFAULT_TAG_LABEL -> {
            Res.strings.default_word
        }
        CartoonTag.Companion.BANGUMI_TAG_LABEL -> {
            Res.strings.bangumi
        }
        else -> label
    }
}