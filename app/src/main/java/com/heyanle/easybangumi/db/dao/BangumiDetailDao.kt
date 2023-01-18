package com.heyanle.easybangumi.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.heyanle.lib_anim.entity.BangumiDetail

/**
 * Created by HeYanLe on 2023/1/18 20:31.
 * https://github.com/heyanLE
 */
@Dao
interface BangumiDetailDao {

    @Insert
    fun insert(detail: BangumiDetail)

    @Update
    fun modify(detail: BangumiDetail)

    @Delete
    fun delete(detail: BangumiDetail)
}