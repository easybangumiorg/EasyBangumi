package com.heyanle.easybangumi4.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import com.heyanle.easy_i18n.R
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.Request

/**
 * Created by HeYanLe on 2023/4/3 22:56.
 * https://github.com/heyanLE
 */
object AnnoHelper {

    private const val checkCD = 30 * 60 * 1000L

    var lastCheckTime by okkv("anno_last_check_time", 0L)
    var lastAnnoName by okkv<String>("last_anno_name", "")
    val annoString = mutableStateOf<String?>(null)

    const val baseUrl =
        "https://raw.githubusercontent.com/easybangumiorg/EasyBangumi-sources/main/announcement/"
    const val lastUrl = "${baseUrl}LATEST.txt"

    private val scope = MainScope()

    fun init() {
        scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val now = System.currentTimeMillis()
                if (now - lastCheckTime >= checkCD) {
                    lastCheckTime = now

                    OkhttpHelper.client.newCall(
                        Request.Builder().url(lastUrl).get().build()
                    ).execute().body?.string()
                        ?.let { name ->
                            if (name.isNotEmpty() && lastAnnoName != name) {

                                val anno = getAnno(name)
                                anno?.let {
                                    lastAnnoName = name
                                    annoString.value = it
                                }

                            }
                        }

                }
            }.onFailure {
                it.printStackTrace()
            }

        }
    }

    private fun getAnno(fileName: String): String? {
        return OkhttpHelper.client.newCall(
            Request.Builder().url("${baseUrl}${fileName}").get().build()
        ).execute().body?.string()
    }

    @Composable
    fun ComposeDialog() {
        LaunchedEffect(key1 = Unit) {
            AnnoHelper.init()
        }

        annoString.value?.let {
            AlertDialog(
                onDismissRequest = {
                    annoString.value = null
                },
                title = {
                    Text(text = stringResource(id = R.string.announcement) + lastAnnoName)
                },
                text = {
                    Text(text = it)
                },
                confirmButton = {
                    TextButton(onClick = {
                        annoString.value = null
                    }) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                }
            )
        }


    }


}