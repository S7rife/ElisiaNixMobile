package ru.feip.elisianix.common.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "searchHistory")
data class SearchQuery(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "query") val query: String,
)

@Entity(tableName = "cart")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "productId") val productId: Int,
    @ColumnInfo(name = "colorId") val colorId: Int,
    @ColumnInfo(name = "sizeId") val sizeId: Int,
    @ColumnInfo(name = "count") val count: Int
)

@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "productId") val productId: Int
)