package ru.feip.elisianix.common.db

import ru.feip.elisianix.common.App
import ru.feip.elisianix.remote.models.ProductDetail
import ru.feip.elisianix.remote.models.ProductMainPreview

val cartDao = App.INSTANCE.db.CartDao()
val favDao = App.INSTANCE.db.FavoritesDao()

fun addItemInCart(item: CartItem) {
    cartDao.insert(CartItem(0, item.productId, item.colorId, item.sizeId, 1))
}

fun deleteItemInCart(item: CartItem) {
    cartDao.deleteByInfo(item.productId, item.colorId, item.sizeId)
}

fun editItemInCart(item: CartItem, inRemote: Boolean? = null) {
    when (inRemote == null) {
        true -> {
            when (checkInCartByInfo(item)) {
                true -> deleteItemInCart(item)
                false -> addItemInCart(item)
            }
        }

        false -> {
            when (inRemote) {
                true -> addItemInCart(item)
                false -> deleteItemInCart(item)
            }
        }
    }
}

fun checkInCartByInfo(item: CartItem): Boolean {
    return App.INSTANCE.db.CartDao().checkInCart(item.productId, item.colorId, item.sizeId) > 0
}

fun checkInCartById(id: Int): Boolean {
    return App.INSTANCE.db.CartDao().checkInCartById(id) > 0
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

fun detailToPreview(it: ProductDetail): ProductMainPreview {
    return ProductMainPreview(
        id = it.id,
        article = it.article,
        name = it.name,
        price = it.price,
        isNew = it.isNew,
        brand = it.brand,
        category = it.category,
        images = it.images,
        colors = it.colors,
        discount = null,
        sizes = it.sizes.filter { it.available > 0 },
        createdDate = null,
        inCart = checkInCartById(it.id),
        inFavorites = checkInFavorites(it.id)
    )
}