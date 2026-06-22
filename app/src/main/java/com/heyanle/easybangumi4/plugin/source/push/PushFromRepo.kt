package com.heyanle.easybangumi4.plugin.source.push

import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CoroutineScope

@Deprecated("Repository-based extension import is retired in plugin V3.")
class PushFromRepo : SourcePushTask {

    override fun identify(): String {
        return SourcePushTask.SOURCE_PUSH_TASK_IDENTIFY_FROM_FILE_REPO
    }

    override suspend fun invoke(
        scope: CoroutineScope,
        param: SourcePushTask.Param,
        container: SourcePushController.SourcePushTaskContainer,
    ) {
        container.dispatchError(stringRes(R.string.load_fail))
    }
}

