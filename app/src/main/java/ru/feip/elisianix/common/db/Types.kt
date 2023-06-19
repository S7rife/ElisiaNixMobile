package ru.feip.elisianix.common.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

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

@Entity(tableName = "users", primaryKeys = ["username", "role"])
data class UserInfo(
    @SerializedName("username")
    @ColumnInfo(name = "username") val username: String,

    @SerializedName("firstName")
    @ColumnInfo(name = "firstName") val firstName: String?,

    @SerializedName("lastName")
    @ColumnInfo(name = "lastName") val lastName: String?,

    @SerializedName("email")
    @ColumnInfo(name = "email") val email: String?,

    @SerializedName("token")
    @ColumnInfo(name = "token") val token: String,

    @SerializedName("role")
    @ColumnInfo(name = "role") val role: String,
)