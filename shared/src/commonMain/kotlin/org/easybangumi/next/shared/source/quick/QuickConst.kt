package org.easybangumi.next.shared.source.quick

import org.easybangumi.next.shared.source.quick.component.QuickComponentWrapper
import org.easybangumi.next.shared.source.quick.component.QuickPlayComponentWrapper
import org.easybangumi.next.shared.source.quick.component.QuickPrefComponentWrapper
import org.easybangumi.next.shared.source.quick.component.QuickSearchComponentWrapper

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
object QuickConst {

    // QuickComponent Factory 接口
    val quickComponentFactoryClazz: Set<QuickComponentWrapper.Factory<*>> = setOf(
        QuickPlayComponentWrapper.Factory(),
        QuickSearchComponentWrapper.Factory(),
        QuickPrefComponentWrapper.Factory(),
    )

}