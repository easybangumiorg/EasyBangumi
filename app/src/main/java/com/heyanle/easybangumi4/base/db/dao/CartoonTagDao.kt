package com.heyanle.easybangumi4.base.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.base.entity.CartoonTag
import kotlinx.coroutines.flow.Flow

/**
 * Created by HeYanLe on 2023/8/6 16:14.
 * https://github.com/heyanLE
 */
@Dao
interface CartoonTagDao {

    @Insert
    suspend fun insert(cartoonTag: CartoonTag)

    @Update
    suspend fun update(cartoonTag: CartoonTag)

    @Delete
    suspend fun delete(cartoonTag: CartoonTag)

    @Query("SELECT * FROM CartoonTag WHERE id=(:id)")
    suspend fun findById(id: Int): CartoonTag?

    @Query("SELECT * FROM CartoonTag")
    fun flowAll(): Flow<List<CartoonTag>>

    @Transaction
    suspend fun updateAll(list: List<CartoonTag>) {
        list.forEach {
            val old = findById(it.id)
            if (old == null) {
                insert(it)
            } else {
                update(it)
            }
        }
    }
}