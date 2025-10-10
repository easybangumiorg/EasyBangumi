package com.heyanle.easybangumi4

import androidx.compose.material3.Text
import com.heyanle.easybangumi4.ui.common.MoeDialogData
import com.heyanle.easybangumi4.ui.common.moeDialogAlert
import com.heyanle.easybangumi4.ui.common.show
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.KtorUtil
import com.heyanle.easybangumi4.utils.openUrl
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by heyanlin on 2025/10/10.
 */
object UpdateController {

    const val URL = "https://api.github.com/repos/easybangumiorg/EasyBangumi/releases"
    const val JUMP_URL = "https://github.com/easybangumiorg/EasyBangumi/releases/latest"
    fun checkUpdate() {
        CoroutineProvider.globalMainScope.launch(Dispatchers.IO) {

            try {
                val text = KtorUtil.client.get {
                    url(URL)
                }.bodyAsText()
                val json = JSONArray(text)
                val latest = json.getJSONObject(0)
                val latestTag = latest.getString("tag_name")
                if (latestTag.isNotEmpty() && latestTag != BuildConfig.VERSION_NAME) {
                    MoeDialogData.AlertDialog(
                        text ={ Text(text = "检测到新版本：$latestTag") },
                        title = null,
                        onDismissBtn = {},
                        onConfirmBtn = {
                            JUMP_URL.openUrl()
                        },
                        dismissLabel = "取消",
                        confirmLabel = "跳转",
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }
}