package org.easybangumi.next.lib.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Created by heyanlin on 2025/4/9.
 */


actual fun CoroutineProvider.main(): CoroutineDispatcher {
    return Dispatchers.Main
}