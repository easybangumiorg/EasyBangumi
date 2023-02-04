package com.heyanle.bangumi_source_api.api

import com.heyanle.bangumi_source_api.api.entity.Bangumi
import com.heyanle.bangumi_source_api.api.entity.BangumiDetail
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import java.lang.Exception


/**
 * Created by HeYanLe on 2021/10/20 20:07.
 * https://github.com/heyanLE
 */

interface ISourceParser {

    sealed class ParserResult<T> {
        data class Complete<T>(
            val data: T
        ) : ParserResult<T>()

        data class Error<T>(
            val throwable: Throwable,
            val isParserError: Boolean = false
        ) : ParserResult<T>()

        inline fun complete(block: (Complete<T>) -> Unit): ParserResult<T> {
            if (this is Complete) {
                block(this)
            }
            return this
        }

        inline fun error(block: (Error<T>) -> Unit): ParserResult<T> {
            if (this is Error) {
                block(this)
            }
            return this
        }
    }

    fun getKey(): String
    fun getLabel(): String
    fun getVersion(): String
    fun getVersionCode(): Int

    fun getDescribe(): String = ""

}

interface IHomeParser : ISourceParser {
    /**
     * 获取首页
     * @return 栏目 番剧列表
     */
    suspend fun home(): ISourceParser.ParserResult<LinkedHashMap<String, List<Bangumi>>>
}

interface ISearchParser : ISourceParser {
    fun firstKey(): Int

    /**
     * 搜索
     * @param keyword 关键字
     * @return 结果 <下一页的 key, 结果>
     */
    suspend fun search(
        keyword: String,
        key: Int
    ): ISourceParser.ParserResult<Pair<Int?, List<Bangumi>>>
}

interface IDetailParser : ISourceParser {
    suspend fun detail(bangumi: BangumiSummary): ISourceParser.ParserResult<BangumiDetail>
}

interface IPlayerParser : ISourceParser {

    data class PlayerInfo(
        val type: Int = TYPE_DASH,
        val uri: String = "",
    ) {
        companion object {
            const val TYPE_DASH = 0
            const val TYPE_HLS = 2
            const val TYPE_OTHER = 4
        }
    }

    /**
     * 播放线路
     */
    suspend fun getPlayMsg(bangumi: BangumiSummary): ISourceParser.ParserResult<LinkedHashMap<String, List<String>>>

    /**
     * 下标
     */
    suspend fun getPlayUrl(
        bangumi: BangumiSummary,
        lineIndex: Int,
        episodes: Int
    ): ISourceParser.ParserResult<PlayerInfo> {
        return ISourceParser.ParserResult.Error(Exception("Unsupported"), true)
    }
}

