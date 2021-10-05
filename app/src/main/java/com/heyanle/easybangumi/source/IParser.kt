package com.heyanle.easybangumi.source

import com.heyanle.easybangumi.entity.Bangumi
import com.heyanle.easybangumi.entity.BangumiDetail

/**
 * 番剧源 接口
 * Created by HeYanLe on 2021/9/8 22:20.
 * https://github.com/heyanLE
 */

interface IParser{
    fun getKey():String
    fun getLabel(): String
}

interface ISearchParser: IParser{
    /**
     * 搜索
     * @param keyword 关键字
     * @param page 页码
     * @return Bangumi 列表
     */
    suspend fun search(keyword: String, page: Int) : List<Bangumi>
}

interface IHomeParser : IParser{

    /**
     * 获取首页
     * @return 栏目名字与对应的 Bangumi
     */
    suspend fun home() : LinkedHashMap<String, List<Bangumi>>
}

interface IBangumiDetailParser : IParser{

    /**
     * 获取番剧详情
     * @param bangumi 番剧实体
     * @return BangumiDetail
     */
    suspend fun detail(bangumi: Bangumi): BangumiDetail?
}

interface IPlayUrlParser : IParser{

    /**
     * 获取播放源数据
     * @param bangumi 番剧实体
     * @return 播放线路名称对应的集的名称
     */
    suspend fun getBangumiPlaySource(bangumi: Bangumi): LinkedHashMap<String, List<String>>

    /**
     * 获取播放 url
     * @param bangumi 番剧实体
     * @param lineIndex 播放线路下标
     * @param episodes 集数
     * @return 播放地址，失败则返回 空字符串
     */
    suspend fun getBangumiPlayUrl(bangumi: Bangumi, lineIndex: Int, episodes: Int): String

}