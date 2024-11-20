package com.heyanle.easybangumi4.ui.common.cover_star

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.navigationCartoonTag
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialog
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialogStar
import com.heyanle.easybangumi4.utils.stringRes

/**
 * Created by heyanlin on 2024/11/19.
 */
@Composable
fun CoverStarDialog(
    show: Boolean,
    cartoon: CartoonCover,
    tagList: List<CartoonTag>,
    onDismissRequest: () -> Unit,
    onStar: (CartoonCover, List<CartoonTag>) -> Unit
) {

    val nav = LocalNavController.current
    if (show) {
        EasyMutiSelectionDialog(
            show = true,
            title = {
                Text(text = stringResource(id = R.string.change_tag))
            },
            items = tagList,
            initSelection = emptyList(),
            confirmText = stringRes(R.string.star),
            onConfirm = {
                onStar(cartoon, it)
            },
            action = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    onClick = {
                        runCatching {
                            nav.navigationCartoonTag()
                        }.onFailure {
                            it.printStackTrace()
                        }
                        onDismissRequest()
                    }
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.edit))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.edit))
                }
            }) {
            onDismissRequest()
        }
    }

}