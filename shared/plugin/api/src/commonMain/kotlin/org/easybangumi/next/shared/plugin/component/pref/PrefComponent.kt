package org.easybangumi.next.shared.plugin.component.pref

import org.easybangumi.next.shared.plugin.component.Component
import org.easybangumi.next.shared.plugin.component.ComponentBundle


/**
 * Created by HeYanLe on 2024/12/8 22:08.
 * https://github.com/heyanLE
 */

interface PrefComponent : Component {

    suspend fun register(): List<SourcePreference>

}

fun ComponentBundle.prefComponent(): PrefComponent?{
    return this.getComponent(PrefComponent::class)
}