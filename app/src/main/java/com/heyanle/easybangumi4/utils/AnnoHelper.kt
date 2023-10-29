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


    var annoList = mutableStateListOf<com.heyanle.easybangumi4.utils.AnnoHelper.AnnoItem>()

    private var showedAnnoListOkkv by okkv("showed_anno_list", "[]")
    private var showedAnnoList: Set<_root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.AnnoItem>
        get() {
            return Gson().fromJson<List<_root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.AnnoItem>>(
                _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.showedAnnoListOkkv,
                object : TypeToken<List<_root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.AnnoItem>>() {}.type
            ).toSet()
        }
        set(value) {
            _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.showedAnnoListOkkv = Gson().toJson(value.toList())
            //showedAnnoListOkkv.loge("AnnoHelper")
        }


    var lastCheckTime by okkv("anno_last_check_time", 0L)

    const val baseUrl =
        "https://raw.githubusercontent.com/easybangumiorg/EasyBangumi-sources/main/announcement/"
    const val lastUrl = "${_root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.baseUrl}LATEST.json"

    private val scope = MainScope()

    fun init() {
        _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val now = System.currentTimeMillis()
                if (now - _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.lastCheckTime >= _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.checkCD || BuildConfig.DEBUG) {
                    _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.lastCheckTime = now

                    _root_ide_package_.com.heyanle.easybangumi4.utils.OkhttpHelper.client.newCall(
                        Request.Builder().url(_root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.lastUrl).get().build()
                    ).execute().body?.string()
                        ?.let { json ->

                            val list = Gson().fromJson<List<_root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.AnnoItem>>(
                                json,
                                object : TypeToken<List<_root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.AnnoItem>>() {}.type
                            )

                            val anno =
                                _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.checkAnno(
                                    list
                                )
                            val d = _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.showedAnnoList.toMutableSet()
                            d.addAll(anno)
                            _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.showedAnnoList = d
                            withContext(Dispatchers.Main) {
                                _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.annoList.clear()
                                _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.annoList.addAll(anno)
                            }
                        }

                }
            }.onFailure {
                it.printStackTrace()
            }

        }
    }

    private fun checkAnno(list: List<_root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.AnnoItem>): List<_root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.AnnoItem> {
        val showed = _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.showedAnnoList
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
            _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.init()
        }

        if (_root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.annoList.isNotEmpty()) {
            val showed = remember(_root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.annoList) {
                _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.annoList.first()
            }
            AlertDialog(
                onDismissRequest = {
                    _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.annoList.remove(showed)
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
                        _root_ide_package_.com.heyanle.easybangumi4.utils.AnnoHelper.annoList.remove(showed)
                    }) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                }
            )
        }


    }


}