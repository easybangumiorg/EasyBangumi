package com.heyanle.easybangumi.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.heyanle.easybangumi.db.entity.BangumiStar

/**
 * Created by HeYanLe on 2023/1/18 21:45.
 * https://github.com/heyanLE
 */
@Dao
interface BangumiStarDao {

    @Insert
    fun insert(bangumiStar: BangumiStar)

    @Update
    fun modify(bangumiStar: BangumiStar)

    @Delete
    fun delete(bangumiStar: BangumiStar)

    @Query("SELECT * FROM BangumiStar ORDER BY createTime DESC")
    fun getAll(): PagingSource<Int, BangumiStar>

    @Query("SELECT * FROM BangumiStar WHERE bangumiId=(:id) AND source=(:source) AND detailUrl = (:detailUrl)")
    fun getBySourceDetailUrl(id: String, source: String, detailUrl: String): BangumiStar?

    @Query("DELETE FROM BangumiStar WHERE bangumiId=(:id) AND source=(:source) AND detailUrl = (:detailUrl)")
    fun deleteByBangumiSummary(id: String, source: String, detailUrl: String)

}