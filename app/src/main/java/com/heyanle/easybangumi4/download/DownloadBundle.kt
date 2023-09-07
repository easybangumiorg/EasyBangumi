package com.heyanle.easybangumi4.download

import com.heyanle.easybangumi4.base.hekv.HeKV
import kotlin.reflect.KProperty

/**
 * 一个下载任务需要用到的数据聚合
 * Created by HeYanLe on 2023/9/3 15:50.
 * https://github.com/heyanLE
 */
class DownloadBundle(
    private val heKV: HeKV,
) {
    var isWorking = false

    var id: Long by hekvLongDelegate("id", -1)
    var cartoonId: String by hekvDelegate("cartoonId", "")
    var cartoonUrl: String by hekvDelegate("cartoonUrl", "")
    var cartoonSource: String by hekvDelegate("cartoonSource", "")

    var cartoonTitle: String by hekvDelegate("cartoonTitle", "")
    var cartoonCover: String by hekvDelegate("cartoonCover", "")
    var playLineLabel: String by hekvDelegate("playLineLabel", "")
    var playLineId: String by hekvDelegate("playLineId", "")
    var playLineEpisodeListJson: String by hekvDelegate("playLineEpisodeListJson", "[]")

    var episodeIndex: Long by hekvLongDelegate("episodeIndex", -1)
    var episodeLabel: String by hekvDelegate("episodeLabel", "")
    var status: Long by hekvLongDelegate("status", 0) //  -1->error 0->init 1->working 2->completely
    var errorMsg: String by hekvDelegate("errorMsg", "")
    // 重试次数只有在恢复任务时允许一次
    var canRetry: Boolean by hekvBooleanDelegate("canRetry", false)

    // 最终视频路径，在中途后缀可能会更改
    var videoPath: String by hekvDelegate("videoPath", "")

    // parsing 填充
    var isParseCompletely: Boolean by hekvBooleanDelegate("isParseCompletely", false)
    var playerInfoUri: String by hekvDelegate("playerInfoUri", "")
    var playerInfoDecodeType: Long by hekvLongDelegate("playerInfoDecodeType", -1)
    var playerInfoHeaderJson: String by hekvDelegate("playerInfoHeadersJson", "{}")

    // aria 填充
    var isAriaCompletely: Boolean by hekvBooleanDelegate("isAriaCompletely", false)
    var ariaId: Long by hekvLongDelegate("ariaId", -1)
    var ariaTargetPath: String by hekvDelegate("ariaTargetPath", "")

    // decrypting 填充
    var isDecryptCompletely: Boolean by hekvBooleanDelegate("isDecryptCompletely", false)
    var tshM3U8Path: String by hekvDelegate("tshM3U8Path", "")


    fun getValue(key: String, def: String): String {
        return heKV.get(key, def)
    }

    fun put(key: String, value: String) {
        heKV.put(key, value)
    }

    private fun hekvDelegate(key: String, value: String) = HekvDelegate(heKV, key, value)
    private fun hekvLongDelegate(key: String, value: Long) = HekvLongDelegate(heKV, key, value)
    private fun hekvBooleanDelegate(key: String, value: Boolean) = HekvBooleanDelegate(heKV, key, value)

    class HekvDelegate(private val heKV: HeKV, private val key: String, private val def: String) {
        operator fun getValue(thisRef: Any, property: KProperty<*>): String {
            return heKV.get(key, def)
        }

        operator fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
            heKV.put(key, value)
        }
    }
    class HekvLongDelegate(private val heKV: HeKV, private val key: String, private val def: Long) {
        operator fun getValue(thisRef: Any, property: KProperty<*>): Long{
            return heKV.get(key, def.toString()).toLongOrNull()?:def
        }

        operator fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
            heKV.put(key, value.toString())
        }
    }
    class HekvBooleanDelegate(private val heKV: HeKV, private val key: String, private val def: Boolean) {
        operator fun getValue(thisRef: Any, property: KProperty<*>): Boolean{
            return heKV.get(key, def.toString()).toBooleanStrictOrNull()?:def
        }

        operator fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
            heKV.put(key, value.toString())
        }
    }


}