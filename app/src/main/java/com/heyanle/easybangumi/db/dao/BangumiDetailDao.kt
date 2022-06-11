package com.heyanle.easybangumi.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.heyanle.easybangumi.entity.BangumiDetail

/**
 * Created by HeYanLe on 2021/9/20 21:32.
 * https://github.com/heyanLE
 */
@Dao
interface BangumiDetailDao {

    @Query("select * from bangumi_detail where id=:id ")
    fun findBangumiDetailById(id: String):List<BangumiDetail>

    @Insert
    fun insert(vararg bangumiDetail: BangumiDetail)

    @Update
    fun update(vararg bangumiDetail: BangumiDetail)

    @Query("select * from bangumi_detail where star = 1 order by lastVisiTime desc")
    fun findStarBangumiDetail():PagingSource<Int, BangumiDetail>

    @Delete
    fun delete(vararg bangumiDetail: BangumiDetail)

}