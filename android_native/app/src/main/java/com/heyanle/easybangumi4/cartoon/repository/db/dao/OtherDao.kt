package com.heyanle.easybangumi4.cartoon.repository.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonInfoV1

/**
 * 其他的 Dao，一般是用于版本迁移
 * Created by heyanle on 2024/1/28.
 * https://github.com/heyanLE
 */
@Dao
interface OtherDao {


    @Query("SELECT * FROM CartoonInfo")
    suspend fun getAllCartoonInfoV1(): List<CartoonInfoV1>


}