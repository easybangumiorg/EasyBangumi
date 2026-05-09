package org.easybangumi.next.source.local

import org.easybangumi.next.shared.local.LocalPreference
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.pref.InnerPrefComponent
import org.easybangumi.next.shared.source.api.component.pref.MediaSourcePreference
import org.koin.core.component.inject

/**
 * 本地源配置组件
 */
class LocalPrefComponent : InnerPrefComponent, BaseComponent() {

    private val localPreference: LocalPreference by inject()

    override fun registerInner(): List<MediaSourcePreference> {
        return listOf(
            MediaSourcePreference.Switch(
                label = "使用私有目录",
                key = LocalPreference.KEY_USE_PRIVATE,
                defBoolean = true,
            ),
            MediaSourcePreference.Switch(
                label = "创建 .nomedia（隐藏相册扫描）",
                key = LocalPreference.KEY_NO_MEDIA,
                defBoolean = true,
            ),
        )
    }
}
