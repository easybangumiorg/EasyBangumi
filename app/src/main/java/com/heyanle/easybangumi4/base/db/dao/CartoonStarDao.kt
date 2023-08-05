package com.heyanle.easybangumi4.base.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.base.entity.CartoonStar
import kotlinx.coroutines.flow.Flow

/**
 * Created by HeYanLe on 2023/3/7 14:57.
 * https://github.com/heyanLE
 */
@Dao
interface CartoonStarDao {

    @Insert
    suspend fun insert(cartoonStar: CartoonStar)

    @Update
    suspend fun update(cartoonStar: CartoonStar)

    @Delete
    suspend fun delete(cartoonStar: CartoonStar)

    @Query("SELECT * FROM CartoonStar ORDER BY createTime DESC")
    suspend fun getAll(): List<CartoonStar>

    @Query("SELECT * FROM CartoonStar ORDER BY createTime DESC")
    fun flowAll(): Flow<List<CartoonStar>>

    @Query("SELECT * FROM CartoonStar WHERE source=(:source)")
    suspend fun getAllBySource(source: String): List<CartoonStar>
    @Query("SELECT * FROM CartoonStar WHERE id=(:id) AND source=(:source) AND url=(:url)")
    suspend fun getByCartoonSummary(id: String, source: String, url: String): CartoonStar?

    @Query("DELETE FROM CartoonStar WHERE id=(:id) AND source=(:source) AND url=(:detailUrl)")
    suspend fun deleteByCartoonSummary(id: String, source: String, detailUrl: String)

    @Transaction
    suspend fun delete(cartoonStar: List<CartoonStar>){
        cartoonStar.forEach {
            delete(it)
        }
    }

    @Transaction
    suspend fun modify(cartoonStar: CartoonStar) {
        val old = getByCartoonSummary(cartoonStar.id, cartoonStar.source, cartoonStar.url)
        if (old == null) {
            insert(cartoonStar.copy(createTime = System.currentTimeMillis()))
        } else {
            update(cartoonStar.copy(createTime = old.createTime))
        }
    }

    @Transaction
    suspend fun modify(starList: List<CartoonStar>, lastUpdateTime: Long? = null) {
        starList.forEach { cartoonStar ->
            val old = getByCartoonSummary(cartoonStar.id, cartoonStar.source, cartoonStar.url)
            if (old == null) {
                insert(cartoonStar.copy(createTime = System.currentTimeMillis(), lastUpdateTime = lastUpdateTime?:cartoonStar.lastUpdateTime))
            } else {
                update(cartoonStar.copy(createTime = old.createTime, lastUpdateTime = lastUpdateTime?:cartoonStar.lastUpdateTime))
            }
        }
    }

    @Transaction
    suspend fun migration(old: List<CartoonStar>, new: List<CartoonStar>){
        old.forEach {
            deleteByCartoonSummary(it.id, it.source, it.url)
        }
        new.forEach {
            modify(it)
        }
    }


}