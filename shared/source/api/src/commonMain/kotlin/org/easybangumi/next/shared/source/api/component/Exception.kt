package org.easybangumi.next.shared.source.api.component


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

open class ComponentException(msg: String): RuntimeException(msg)

class NeedWebViewCheck(val url: String,): ComponentException("需要启动网页效验: $url")