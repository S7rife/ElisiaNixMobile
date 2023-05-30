package ru.feip.elisianix.common.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM searchHistory ORDER BY id DESC")
    fun getAll(): LiveData<List<SearchQuery>>

    @Query("SELECT * FROM searchHistory WHERE id=:id")
    fun getById(id: Int): SearchQuery

    @Insert
    fun insert(searchQuery: SearchQuery)

    @Insert
    fun insertAll(vararg searchQuery: SearchQuery)

    @Delete
    fun delete(searchQuery: SearchQuery)

    @Query(
        "DELETE FROM searchHistory " +
                "WHERE id <= (" +
                "SELECT id FROM (SELECT * FROM searchHistory) as tmp " +
                "ORDER BY id DESC " +
                "LIMIT 4,1)"
    )
    fun deleteExcept()
}