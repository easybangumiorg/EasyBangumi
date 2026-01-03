package org.easybangumi.next.shared.foundation

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import org.easybangumi.next.lib.utils.ResourceOr

/**
 * Created by heyanle on 2025/3/3.
 */
@Composable
fun stringRes(resource: ResourceOr): String{
    if (resource is StringResource) {
        return stringResource(resource)
    }
    return resource.toString()
}

@Composable
fun stringRes(resource: ResourceOr, vararg args: Any): String {
    if (resource is StringResource) {
        return stringResource(resource, args)
    }
    return resource.toString()
}