package org.easybangumi.next.source.inner.ggl

import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.pref.InnerPrefComponent
import org.easybangumi.next.shared.source.api.component.pref.MediaSourcePreference
import org.easybangumi.next.shared.source.api.component.pref.PrefComponent
import org.easybangumi.next.shared.source.api.source.Source

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
class GGLPrefComponent: InnerPrefComponent, BaseComponent() {

    override fun registerInner(): List<MediaSourcePreference>  = listOf(
        MediaSourcePreference.Edit("域名", "host", "bgm.girigirilove.com")
    )


}