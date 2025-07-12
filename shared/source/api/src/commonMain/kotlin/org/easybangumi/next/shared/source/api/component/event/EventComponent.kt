package org.easybangumi.next.shared.source.api.component.event

import org.easybangumi.next.shared.source.api.component.Component

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

interface EventComponent : Component {

    val event: Array<String>

    fun onEvent(event: String, vararg args: Any)

}