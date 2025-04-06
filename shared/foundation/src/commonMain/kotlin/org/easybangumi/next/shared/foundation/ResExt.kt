package org.easybangumi.next.shared.foundation

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import org.easybangumi.next.shared.resources.ResourceOr

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