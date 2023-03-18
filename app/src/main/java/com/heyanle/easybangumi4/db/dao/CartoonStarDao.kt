package com.heyanle.easybangumi4.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.heyanle.easybangumi4.db.entity.CartoonStar

/**
 * Created by HeYanLe on 2023/3/7 14:57.
 * https://github.com/heyanLE
 */
@Dao
interface CartoonStarDao {

    @Insert
    fun insert(cartoonStar: CartoonStar)

    @Update
    fun modify(cartoonStar: CartoonStar)

    @Delete
    fun delete(cartoonStar: CartoonStar)

    @Query("SELECT * FROM CartoonStar ORDER BY createTime DESC")
    fun getAll(): PagingSource<Int, CartoonStar>

    @Query("SELECT count(*) FROM CartoonStar")
    fun countAll():Int

    @Query("SELECT * FROM CartoonStar WHERE title LIKE '%' || :searchKey || '%' ORDER BY createTime DESC")
    fun getSearch(searchKey: String): PagingSource<Int, CartoonStar>

    @Query("SELECT * FROM CartoonStar WHERE id=(:id) AND source=(:source) AND url = (:detailUrl)")
    fun getBySourceDetailUrl(id: String, source: String, detailUrl: String): CartoonStar?

    @Query("DELETE FROM CartoonStar WHERE id=(:id) AND source=(:source) AND url = (:detailUrl)")
    fun deleteByCartoonSummary(id: String, source: String, detailUrl: String)


}