package com.heyanle.easybangumi4.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.db.entity.CartoonStar
import kotlinx.coroutines.flow.Flow

/**
 * Created by HeYanLe on 2023/3/7 14:57.
 * https://github.com/heyanLE
 */
@Dao
interface CartoonStarDao {

    @Insert
    fun insert(cartoonStar: CartoonStar)

    @Update
    fun update(cartoonStar: CartoonStar)

    @Delete
    fun delete(cartoonStar: CartoonStar)

    @Query("SELECT * FROM CartoonStar ORDER BY createTime DESC")
    fun getAll(): List<CartoonStar>

    @Query("SELECT * FROM CartoonStar ORDER BY createTime DESC")
    fun flowAll(): Flow<List<CartoonStar>>

    @Query("SELECT * FROM CartoonStar ORDER BY createTime DESC")
    fun pageAll(): PagingSource<Int, CartoonStar>

    @Query("SELECT count(*) FROM CartoonStar")
    fun countAll(): Int

    @Query("SELECT * FROM CartoonStar WHERE title LIKE '%' || :search || '%' ORDER BY createTime DESC")
    fun pageSearch(search: String): PagingSource<Int, CartoonStar>

    @Query("SELECT count(*) FROM CartoonStar WHERE title LIKE '%' || :search || '%'")
    fun countSearch(search: String): Int


    @Query("SELECT * FROM CartoonStar WHERE id=(:id) AND source=(:source) AND url=(:url)")
    fun getByCartoonSummary(id: String, source: String, url: String): CartoonStar?

    @Query("DELETE FROM CartoonStar WHERE id=(:id) AND source=(:source) AND url=(:detailUrl)")
    fun deleteByCartoonSummary(id: String, source: String, detailUrl: String)

    @Transaction
    fun delete(cartoonStar: List<CartoonStar>){
        cartoonStar.forEach {
            delete(it)
        }
    }

    @Transaction
    fun modify(cartoonStar: CartoonStar) {
        val old = getByCartoonSummary(cartoonStar.id, cartoonStar.source, cartoonStar.url)
        if (old == null) {
            insert(cartoonStar.copy(createTime = System.currentTimeMillis()))
        } else {
            update(cartoonStar.copy(createTime = old.createTime))
        }
    }

    @Transaction
    fun modify(starList: List<CartoonStar>, lastUpdateTime: Long? = null) {
        starList.forEach { cartoonStar ->
            val old = getByCartoonSummary(cartoonStar.id, cartoonStar.source, cartoonStar.url)
            if (old == null) {
                insert(cartoonStar.copy(createTime = System.currentTimeMillis(), lastUpdateTime = lastUpdateTime?:cartoonStar.lastUpdateTime))
            } else {
                update(cartoonStar.copy(createTime = old.createTime, lastUpdateTime = lastUpdateTime?:cartoonStar.lastUpdateTime))
            }
        }
    }


}