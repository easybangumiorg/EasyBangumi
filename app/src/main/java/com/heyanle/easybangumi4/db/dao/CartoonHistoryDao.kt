package com.heyanle.easybangumi4.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.db.entity.CartoonHistory

/**
 * Created by HeYanLe on 2023/3/7 14:57.
 * https://github.com/heyanLE
 */
@Dao
interface CartoonHistoryDao {

    @Insert
    fun insert(cartoonHistory: CartoonHistory)

    @Update
    fun update(cartoonHistory: CartoonHistory)

    @Delete
    fun delete(cartoonHistory: CartoonHistory)

    @Query("DELETE FROM CartoonHistory WHERE 1=1")
    fun clear()

    @Query("SELECT * FROM cartoonHistory ORDER BY createTime DESC")
    fun getAllOrderByTime(): PagingSource<Int, CartoonHistory>

    @Query("SELECT * FROM cartoonHistory WHERE name LIKE '%' || :searchKey || '%' ORDER BY createTime DESC")
    fun getSearchOrderByTime(searchKey: String): PagingSource<Int, CartoonHistory>

    @Query("SELECT * FROM cartoonHistory WHERE id=(:id) AND source=(:source) AND url=(:detailUrl)")
    fun getFromCartoonSummary(id: String, source: String, detailUrl: String): CartoonHistory?

    @Query("DELETE FROM cartoonHistory WHERE id=(:id) AND source=(:source) AND url=(:detailUrl)")
    fun deleteByCartoonSummary(id: String, source: String, detailUrl: String)

    @Transaction
    fun delete(cartoonHistory: List<CartoonHistory>){
        cartoonHistory.forEach {
            delete(it)
        }
    }

    @Transaction
    fun modify(cartoonHistory: CartoonHistory) {
        val old = getFromCartoonSummary(cartoonHistory.id, cartoonHistory.source, cartoonHistory.url)
        if (old == null) {
            insert(cartoonHistory.copy(createTime = System.currentTimeMillis()))
        } else {
            update(cartoonHistory.copy(createTime = old.createTime))
        }
    }

}