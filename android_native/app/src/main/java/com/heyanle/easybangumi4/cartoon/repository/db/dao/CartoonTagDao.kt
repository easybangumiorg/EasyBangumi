package com.heyanle.easybangumi4.cartoon.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonTagOld
import kotlinx.coroutines.flow.Flow

/**
 * Created by HeYanLe on 2023/8/6 16:14.
 * https://github.com/heyanLE
 */
@Deprecated("")
@Dao
interface CartoonTagDao {

    @Insert
    suspend fun insert(cartoonTag: CartoonTagOld)

    @Update
    suspend fun update(cartoonTag: CartoonTagOld)

    @Delete
    suspend fun delete(cartoonTag: CartoonTagOld)

    @Query("SELECT * FROM CartoonTag WHERE id=(:id)")
    suspend fun findById(id: Int): CartoonTagOld?

    @Query("SELECT * FROM CartoonTag WHERE label=(:label)")
    suspend fun findByLabel(label: String): List<CartoonTagOld>

    @Query("SELECT * FROM CartoonTag")
    fun flowAll(): Flow<List<CartoonTagOld>>

    @Query("SELECT * FROM CartoonTag")
    fun getAll(): List<CartoonTagOld>

    @Query("DELETE FROM CartoonTag WHERE 1=1")
    suspend fun clear()

    @Transaction
    suspend fun updateAll(list: List<CartoonTagOld>) {
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