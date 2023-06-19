package ru.feip.elisianix.common.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SearchQuery::class, CartItem::class, FavoriteItem::class, UserInfo::class],
    version = 1
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun CartDao(): CartDao
    abstract fun FavoritesDao(): FavoritesDao
    abstract fun UserInfoDao(): UserInfoDao
}