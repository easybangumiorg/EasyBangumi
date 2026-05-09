package org.easybangumi.next.source.inner.anich

import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.pref.InnerPrefComponent
import org.easybangumi.next.shared.source.api.component.pref.MediaSourcePreference

class AniChPrefComponent : InnerPrefComponent, BaseComponent() {

    override fun registerInner(): List<MediaSourcePreference> = listOf(
        MediaSourcePreference.Edit("网站域名", "web_host", "anich.emmmm.eu.org")
    )
}
