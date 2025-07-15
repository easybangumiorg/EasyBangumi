package org.easybangumi.next.shared.source.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.source.api.source.SourceInfo
import org.easybangumi.next.shared.source.api.source.SourceType

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

interface SourceProvider {

    val type: SourceType

    val flow: Flow<DataState<List<SourceInfo>>>

}