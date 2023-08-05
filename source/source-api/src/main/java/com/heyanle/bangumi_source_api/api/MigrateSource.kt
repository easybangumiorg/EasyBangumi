package com.heyanle.bangumi_source_api.api

import com.heyanle.bangumi_source_api.api.entity.CartoonSummary

/**
 * Created by HeYanLe on 2023/8/5 17:22.
 * https://github.com/heyanLE
 */
interface MigrateSource: Source {

    /**
     * @param oldVersionCode 旧版的 source version code
     * @return 是否迁移番剧
     */
    fun needMigrate(oldVersionCode: Int): Boolean {
        return false
    }

    /**
     * 迁移后会重新调用 detailedComponent 获取新的详情
     * 如果迁移到一半用户杀进程，则再次启动会再次触发迁移，此时可能会传入部分已迁移的数据
     * @param oldCartoonSummaryList 旧版中收藏番的摘要
     * @param oldVersionCode 旧版的 source version code
     * @return 迁移后的 map
     */
    suspend fun onMigrate(oldCartoonSummaryList: List<CartoonSummary>, oldVersionCode: Int):  List<CartoonSummary> {
        throw IllegalStateException("source no onMigrate: (${this})")
    }

}