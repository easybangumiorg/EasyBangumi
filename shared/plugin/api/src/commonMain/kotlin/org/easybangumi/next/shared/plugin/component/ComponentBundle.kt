package org.easybangumi.next.shared.plugin.component

import org.easybangumi.next.shared.plugin.source.Source
import kotlin.reflect.KClass


/**
 * Created by HeYanLe on 2024/12/8 22:18.
 * https://github.com/heyanLE
 */

interface ComponentBundle {

    fun getSource(): Source

    fun <T: Component> getComponent(clazz: KClass<T>): T?



}