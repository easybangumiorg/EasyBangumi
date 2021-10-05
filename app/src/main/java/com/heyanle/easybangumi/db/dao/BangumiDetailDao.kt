package com.heyanle.easybangumi.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

}