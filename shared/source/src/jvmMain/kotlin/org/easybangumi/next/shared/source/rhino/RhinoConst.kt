package org.easybangumi.next.shared.source.rhino

import org.easybangumi.next.shared.source.rhino.component.RhinoComponentWrapper

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
object RhinoConst {

    // Rhino Component Factory 接口
    val rhinoComponentFactoryClazz: Set<RhinoComponentWrapper.Factory<*>> = setOf(
//        RhinoPlayComponentWrapper.Factory(),
    )

    internal val RHINO_IMPORT_STRING: String by lazy {
        """
          
          
         """.trimIndent().trim()
    }


}