package org.easybangumi.next.source.local

import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.source.api.component.Component
import org.easybangumi.next.shared.source.api.source.InnerSource

/**
 * 本地番剧源
 * key: easybangumi_local
 */
class LocalInnerSource : InnerSource() {

    companion object {
        const val SOURCE_KEY = "easybangumi_local"
    }

    override val key: String = SOURCE_KEY
    override val label: ResourceOr = "本地番剧"
    override val icon: ResourceOr = Res.images.logo
    override val version: Int = 1

    override val componentConstructor: Array<() -> Component> = arrayOf(
        ::LocalPlayComponent,
        ::LocalPrefComponent,
    )
}
