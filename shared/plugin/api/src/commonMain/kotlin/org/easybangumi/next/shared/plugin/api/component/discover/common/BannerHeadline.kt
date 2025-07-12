package org.easybangumi.next.shared.plugin.api.component.discover.common

import org.easybangumi.next.lib.utils.ResourceOr

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
data class BannerHeadline (
    val label: ResourceOr,
    val hasTimelineEnter: Boolean = false,
)