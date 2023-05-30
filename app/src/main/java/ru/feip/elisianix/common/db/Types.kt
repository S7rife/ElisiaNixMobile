package ru.feip.elisianix.common.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "searchHistory")
data class SearchQuery(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "query") val query: String,
)