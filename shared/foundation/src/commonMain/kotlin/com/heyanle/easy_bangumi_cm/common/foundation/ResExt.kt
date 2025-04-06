package com.heyanle.easy_bangumi_cm.common.foundation

import androidx.compose.runtime.Composable
import com.heyanle.easy_bangumi_cm.base.utils.resources.ResourceOr
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource

/**
 * Created by heyanlin on 2025/3/3.
 */
@Composable
fun stringRes(resource: ResourceOr): String {
    if (resource is StringResource) {
        return stringResource(resource)
    }
    return resource.toString()
}