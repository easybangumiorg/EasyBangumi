package org.easybangumi.next.shared.source.core.source

import kotlinx.coroutines.CoroutineScope
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.shared.source.api.source.Source
import org.easybangumi.next.shared.source.api.source.SourceManifest

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

class ManifestSource(
    override val manifest: SourceManifest
): Source {

    override val workPath: UFD
        get() = throw IllegalStateException("workPath not supported in ManifestSource, please use SourceWrapper")
    override val scope: CoroutineScope
        get() = throw IllegalStateException("scope not supported in ManifestSource, please use SourceWrapper")
}