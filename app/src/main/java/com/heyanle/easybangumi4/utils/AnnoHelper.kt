package com.heyanle.easybangumi4.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

/**
 * Created by HeYanLe on 2023/4/3 22:56.
 * https://github.com/heyanLE
 */
object AnnoHelper {

    private const val checkCD = 30 * 60 * 1000L

    data class AnnoItem(
        var title: String,
        var content: String,
        @SerializedName("publish_time")
        var publishTime: String,
        @SerializedName("version_code")
        var versionCode: String,
    ) {

    }


    var annoList = mutableStateListOf<AnnoItem>()

    private var showedAnnoListOkkv by okkv("showed_anno_list", "[]")
    private var showedAnnoList: Set<AnnoItem>
        get() {
            return Gson().fromJson<List<AnnoItem>>(
                showedAnnoListOkkv,
                object : TypeToken<List<AnnoItem>>() {}.type
            ).toSet()
        }
        set(value) {
            showedAnnoListOkkv = Gson().toJson(value.toList())
            //showedAnnoListOkkv.loge("AnnoHelper")
        }


    var lastCheckTime by okkv("anno_last_check_time", 0L)

    const val baseUrl =
        "https://raw.githubusercontent.com/easybangumiorg/EasyBangumi-sources/main/announcement/"
    const val lastUrl = "${baseUrl}LATEST.json"

    private val scope = MainScope()

    fun init() {
        scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val now = System.currentTimeMillis()
                if (now - lastCheckTime >= checkCD || BuildConfig.DEBUG) {
                    lastCheckTime = now

                    OkhttpHelper.client.newCall(
                        Request.Builder().url(lastUrl).get().build()
                    ).execute().body?.string()
                        ?.let { json ->

                            val list = Gson().fromJson<List<AnnoItem>>(
                                json,
                                object : TypeToken<List<AnnoItem>>() {}.type
                            )

                            val anno =
                                checkAnno(
                                    list
                                )
                            val d = showedAnnoList.toMutableSet()
                            d.addAll(anno)
                            showedAnnoList = d
                            withContext(Dispatchers.Main) {
                                annoList.clear()
                                annoList.addAll(anno)
                            }
                        }

                }
            }.onFailure {
                it.printStackTrace()
            }

        }
    }

    private fun checkAnno(list: List<AnnoItem>): List<AnnoItem> {
        val showed = showedAnnoList
//        showed.forEach {
//            it.loge("AnnoHelper")
//        }
        return list.filter {
            // 版本检查
            var isMatch = false
            for (v in it.versionCode.split("|")) {
                try {
                    val dd = v.split("~")
                    val startString = dd[0]
                    val endString = dd[1]
                    val start = if (startString == "*") 0 else startString.toInt()
                    val end = if (endString == "*") Int.MAX_VALUE else endString.toInt()
                    if (BuildConfig.VERSION_CODE in start..end) {
                        isMatch = true
                        break
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            isMatch
        }.filter {
            // 是否展示检查
            showed.contains(it).loge("2AnnoHelper")
            !showed.contains(it)
        }
    }

    @Composable
    fun ComposeDialog() {
        LaunchedEffect(key1 = Unit) {
            init()
        }

        if (annoList.isNotEmpty()) {
            val showed = remember(annoList) {
                annoList.first()
            }
            AlertDialog(
                onDismissRequest = {
                    annoList.remove(showed)
                },
                title = {
                    Text(text = stringResource(id = R.string.announcement) + " " + showed.title)
                },
                text = {
                    Column {
                        Text(text = showed.publishTime)
                        Text(text = showed.content)
                    }

                },
                confirmButton = {
                    TextButton(onClick = {
                        annoList.remove(showed)
                    }) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                }
            )
        }


    }


}