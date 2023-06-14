package ru.feip.elisianix.common.db

import ru.feip.elisianix.common.App
import ru.feip.elisianix.remote.models.CartItemRemote
import ru.feip.elisianix.remote.models.ProductMainPreview

fun <T> editItemInCart(item: T) {
    val dao = App.INSTANCE.db.CartDao()
    when (item) {
        is ProductMainPreview -> when (item.inCart) {
            true -> dao.deleteByInfo(item.id, item.colors[0].id, item.sizes[0].id)
            false -> dao.insert(CartItem(0, item.id, item.colors[0].id, item.sizes[0].id, 1))
        }

        is CartItemRemote -> when (item.inCart) {
            true -> dao.deleteByInfo(item.id, item.productColor.id, item.productSize.id)
            false -> dao.insert(
                CartItem(0, item.productId, item.productColor.id, item.productSize.id, 1)
            )
        }

        is CartItem -> when (dao.checkInCart(item.productId, item.colorId, item.sizeId) > 0) {
            true -> dao.deleteByInfo(item.productId, item.colorId, item.sizeId)
            false -> dao.insert(item)
        }

        is Int -> when (dao.checkInCartById(item) > 0) {
            true -> dao.deleteById(item)
            false -> {}
        }
    }
}

fun <T> checkInCart(item: T): Boolean {
    val dao = App.INSTANCE.db.CartDao()
    return when (item) {
        is ProductMainPreview -> dao.checkInCart(item.id, item.colors[0].id, item.sizes[0].id) > 0
        is CartItemRemote -> dao
            .checkInCart(item.productId, item.productColor.id, item.productSize.id) > 0

        is CartItem -> dao.checkInCart(item.productId, item.colorId, item.sizeId) > 0

        is Int -> dao.checkInCartById(item) > 0
        else -> false
    }
}

fun editItemInFavorites(item: Int) {
    val dao = App.INSTANCE.db.FavoritesDao()
    when (dao.checkInFavoritesById(item) > 0) {
        true -> dao.deleteById(item)
        false -> dao.insert(FavoriteItem(0, item))
    }
}

fun checkInFavorites(item: Int): Boolean {
    return App.INSTANCE.db.FavoritesDao().checkInFavoritesById(item) > 0
}