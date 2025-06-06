package org.easybangumi.next.shared.plugin.core.javascript.rhino

import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.api.component.filter.FilterComponent
import org.easybangumi.next.shared.plugin.api.component.play.PlayComponent
import org.easybangumi.next.shared.plugin.api.component.pref.PrefComponent
import org.easybangumi.next.shared.plugin.core.javascript.rhino.component.wrapper.RhinoDiscoverComponentWrapper
import org.easybangumi.next.shared.plugin.core.javascript.rhino.component.wrapper.RhinoPlayComponentWrapper
import org.easybangumi.next.shared.plugin.core.javascript.rhino.component.wrapper.RhinoSearchComponentWrapper
import org.easybangumi.next.shared.plugin.core.javascript.rhino.component.wrapper.RhinoWrapperFactory
import kotlin.reflect.KClass

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
object RhinoConstClazz {

    // Rhino Component Factory 接口
    val rhinoComponentFactoryClazz: Set<RhinoWrapperFactory<*>> = setOf(
        RhinoDiscoverComponentWrapper.Factory(),
        RhinoSearchComponentWrapper.Factory(),
        RhinoPlayComponentWrapper.Factory(),
    )

}