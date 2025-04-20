package org.easybangumi.next.shared.plugin.core.inner

import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.shared.plugin.api.source.SourceWrapper

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

class InnerSourceWrapper(
    innerSource: InnerSource,
    override val workPath: UFD
) : SourceWrapper(innerSource)