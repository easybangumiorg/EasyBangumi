package com.heyanle.easybangumi4.splash.step

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.utils.IntentHelper
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toast

/**
 * Created by heyanle on 2024/7/6.
 * https://github.com/heyanLE
 */
class PermissionStep: BaseStep {

    override val name: String
        get() = "permission"
    override val version: Int
        get() = 1

    @Composable
    override fun Compose() {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp, 16.dp)
        ) {
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                headlineContent = {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.get_app_list_permission))
                },
                supportingContent = {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.get_app_list_permission_msg))
                },
                trailingContent = {
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                        onClick = {
                            IntentHelper.openAppDetailed(APP.packageName, APP)
                            stringRes(com.heyanle.easy_i18n.R.string.get_app_list_permission_toast).toast()
                        }
                    ) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.app_detailed))
                    }
                }
            )

            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                headlineContent = {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.notification_permission))
                },
                supportingContent = {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.notification_permission_msg))
                },
                trailingContent = {
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                        onClick = {
                            IntentHelper.openAppDetailed(APP.packageName, APP)
                            stringRes(com.heyanle.easy_i18n.R.string.notification_permission_toast).toast()
                        }
                    ) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.app_detailed))
                    }
                }
            )
        }
    }
}