package com.heyanle.easybangumi4

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import com.heyanle.easybangumi4.utils.stringRes

/**
 * Created by HeYanLe on 2023/4/4 21:11.
 * https://github.com/heyanLE
 */
object C {

    const val extensionUrl = "https://easybangumi.org/extensions/"

    sealed class About {

        data class Copy(
            val icon: Any?,
            val title: String,
            val msg: String,
            val copyValue: String,
        ): About()

        data class Url(
            val icon: Any?,
            val title: String,
            val msg: String,
            val url: String,
        ): About()
    }

    val aboutList: List<About> by lazy {
        listOf<About>(
            About.Url(
                icon = Icons.Filled.Public,
                title = stringRes(com.heyanle.easy_i18n.R.string.website),
                msg = stringRes(com.heyanle.easy_i18n.R.string.click_to_explore),
                url = "https://easybangumi.org"
            ),
            About.Copy(
                icon = R.drawable.qq,
                title = stringRes(com.heyanle.easy_i18n.R.string.qq_groud),
                msg = "729848189",
                copyValue = "729848189"
            ),
            About.Copy(
                icon = R.drawable.qq,
                title = stringRes(com.heyanle.easy_i18n.R.string.qq_groud),
                msg = "765995255",
                copyValue = "765995255"
            ),
            About.Url(
                icon = R.drawable.qq,
                title = stringRes(com.heyanle.easy_i18n.R.string.qq_chanel),
                msg = stringRes(com.heyanle.easy_i18n.R.string.click_to_add),
                url = "https://pd.qq.com/s/4q8rd0285"
            ),
            About.Url(
                icon = R.drawable.telegram,
                title = stringRes(com.heyanle.easy_i18n.R.string.telegram),
                msg = stringRes(com.heyanle.easy_i18n.R.string.click_to_add),
                url = "https://t.me/easybangumi"
            ),
            About.Url(
                icon = R.drawable.github,
                title = stringRes(com.heyanle.easy_i18n.R.string.github),
                msg = stringRes(com.heyanle.easy_i18n.R.string.click_to_explore),
                url = "https://github.com/easybangumiorg/EasyBangumi"
            ),
        )
    }

}