package org.easybangumi.next.shared.plugin.debug

import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.plugin.api.component.Component
import org.easybangumi.next.shared.plugin.api.inner.InnerSource
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
class DebugSource : InnerSource() {

    override val id: String = "DEBUG"
    override val label: ResourceOr = Res.strings.debug_source
    override val icon: ResourceOr? = Res.images.logo
    override val version: Int = 1
    override val componentConstructor: Array<() -> Component> = arrayOf(
        ::HomeBaseComponent
    )
}