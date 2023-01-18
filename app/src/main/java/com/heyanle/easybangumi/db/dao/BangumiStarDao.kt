package com.heyanle.easybangumi.db.dao

import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.heyanle.easybangumi.db.entity.BangumiStar
import com.heyanle.easybangumi.db.entity.SearchHistory

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

    @Query("SELECT * FROM BangumiStar WHERE source=(:source) AND detailUrl = (:detailUrl)")
    fun getBySourceDetailUrl(source: String, detailUrl: String): BangumiStar?

    @Query("DELETE FROM BangumiStar WHERE source=(:source) AND detailUrl = (:detailUrl)")
    fun deleteBySourceDetailUrl(source: String, detailUrl: String)

}