package com.heyanle.easybangumi4.v2.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.C
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.openUrl
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader
import com.heyanle.easybangumi4.v2.ui.component.V2Section
import com.heyanle.easybangumi4.v2.ui.component.V2SectionDivider

@Composable
internal fun AboutV2() {
    val navController = LocalNavController.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground)
            .navigationBarsPadding(),
    ) {
        V2SecondaryHeader(
            title = stringResource(R.string.about),
            onBack = navController::popBackStack,
            largeTitle = true,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
        ) {
            AboutBrandV2()
            V2Section(title = "应用信息") {
                AboutStaticRowV2(
                    icon = Icons.Filled.AutoAwesome,
                    title = stringResource(R.string.version),
                    value = BuildConfig.VERSION_NAME,
                )
            }
            V2Section(title = "项目与联系") {
                C.aboutList.forEachIndexed { index, item ->
                    if (index > 0) V2SectionDivider()
                    AboutLinkRowV2(
                        item = item,
                        onClick = {
                            when (item) {
                                is C.About.Url -> runCatching { item.url.openUrl() }
                                is C.About.Copy -> {
                                    clipboardManager.setText(AnnotatedString(item.copyValue))
                                    "复制成功".moeSnackBar()
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutBrandV2() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OkImage(
            image = com.heyanle.easybangumi4.R.mipmap.logo_new,
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(76.dp),
        )
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(top = 12.dp),
            color = V2Tokens.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "简单、清晰地管理你的番剧",
            modifier = Modifier.padding(top = 5.dp),
            color = V2Tokens.TextSecondary,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun AboutStaticRowV2(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            color = V2Theme.colors.accentContainer,
            shape = RoundedCornerShape(9.dp),
        ) {
            Icon(icon, contentDescription = null, tint = V2Theme.colors.accent, modifier = Modifier.padding(7.dp))
        }
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            color = V2Tokens.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(value, color = V2Tokens.TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun AboutLinkRowV2(
    item: C.About,
    onClick: () -> Unit,
) {
    val icon = when (item) {
        is C.About.Copy -> item.icon
        is C.About.Url -> item.icon
    }
    val title = when (item) {
        is C.About.Copy -> item.title
        is C.About.Url -> item.title
    }
    val message = when (item) {
        is C.About.Copy -> item.msg
        is C.About.Url -> item.msg
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            color = V2Theme.colors.accentContainer,
            shape = RoundedCornerShape(9.dp),
        ) {
            OkImage(
                image = icon,
                contentDescription = title,
                crossFade = false,
                tint = V2Theme.colors.accent,
                modifier = Modifier.padding(7.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(title, color = V2Tokens.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (message.isNotBlank()) {
                Text(message, modifier = Modifier.padding(top = 3.dp), color = V2Tokens.TextSecondary, fontSize = 12.sp)
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = V2Tokens.TextSecondary,
        )
    }
}
