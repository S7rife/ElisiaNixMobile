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

@Dao
interface CartDao {
    @Query("SELECT * FROM cart ORDER BY id DESC")
    fun getAll(): List<CartItem>

    @Query("SELECT * FROM cart ORDER BY id DESC")
    fun getAllLive(): LiveData<List<CartItem>>

    @Query("SELECT * FROM cart WHERE id=:id")
    fun getById(id: Int): CartItem

    @Query(
        "SELECT COUNT(*) FROM cart " +
                "WHERE productId=:productId " +
                "AND colorId=:colorId " +
                "AND sizeId=:sizeId"
    )
    fun checkInCart(productId: Int, colorId: Int, sizeId: Int): Int

    @Query("SELECT COUNT(*) FROM cart WHERE productId=:productId")
    fun checkInCartById(productId: Int): Int

    @Query("SELECT COUNT(*) FROM cart")
    fun checkCntLive(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM cart")
    fun checkCnt(): Int

    @Insert
    fun insert(cartItem: CartItem)

    @Insert
    fun insertAll(vararg cartItem: CartItem)

    @Delete
    fun delete(cartItem: CartItem)

    @Query(
        "DELETE FROM cart " +
                "WHERE productId=:productId " +
                "AND colorId=:colorId " +
                "AND sizeId=:sizeId"
    )
    fun deleteByInfo(productId: Int, colorId: Int, sizeId: Int): Int

    @Query("DELETE FROM cart WHERE productId=:productId")
    fun deleteById(productId: Int): Int
}

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites ORDER BY id DESC")
    fun getAll(): List<FavoriteItem>

    @Query("SELECT * FROM favorites ORDER BY id DESC")
    fun getAllLive(): LiveData<List<FavoriteItem>>

    @Query("SELECT * FROM favorites WHERE id=:id")
    fun getById(id: Int): FavoriteItem

    @Query("SELECT COUNT(*) FROM favorites WHERE productId=:productId")
    fun checkInFavoritesById(productId: Int): Int

    @Query("SELECT COUNT(*) FROM favorites")
    fun checkCntLive(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM favorites")
    fun checkCnt(): Int

    @Insert
    fun insert(favoriteItem: FavoriteItem)

    @Insert
    fun insertAll(vararg favoriteItem: FavoriteItem)

    @Delete
    fun delete(favoriteItem: FavoriteItem)

    @Query("DELETE FROM favorites WHERE productId=:productId")
    fun deleteById(productId: Int): Int
}