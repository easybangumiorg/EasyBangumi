package com.heyanle.easybangumi.db.dao

import androidx.room.*
import com.heyanle.easybangumi.db.entity.SearchHistory

/**
 * Created by LoliBall on 2022/12/16 22:18.
 * https://github.com/WhichWho
 */
@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM SearchHistory WHERE content=(:content)")
    fun get(content: String): SearchHistory?

    @Query("SELECT * FROM SearchHistory ORDER BY timestamp DESC")
    fun getAll(): List<SearchHistory>

    @Query("SELECT content FROM SearchHistory ORDER BY timestamp DESC LIMIT 10")
    fun getAllContent(): List<String>

    @Insert
    fun insert(historyBean: SearchHistory)

    @Update
    fun modify(historyBean: SearchHistory)

    @Delete
    fun delete(historyBean: SearchHistory)

    @Query("DELETE FROM SearchHistory")
    fun deleteAll()

    @Transaction
    fun insertOrModify(content: String) {
        val query = get(content)
        if (query != null) {
            modify(query.copy(timestamp = System.currentTimeMillis()))
        } else {
            insert(SearchHistory(timestamp = System.currentTimeMillis(), content = content))
        }
    }

}