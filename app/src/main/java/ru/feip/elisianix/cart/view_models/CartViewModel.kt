package ru.feip.elisianix.cart.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.db.checkInCart
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.common.db.detailToPreview
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.Cart
import ru.feip.elisianix.remote.models.CartItemRemote
import ru.feip.elisianix.remote.models.ProductMainPreview
import ru.feip.elisianix.remote.models.RequestProductCart
import ru.feip.elisianix.remote.models.sortPreviewsItems

class CartViewModel : ViewModel() {
    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _cart = MutableSharedFlow<Cart>(replay = 1)
    private val _likedProducts = MutableSharedFlow<List<ProductMainPreview>>(replay = 1)

    val showLoading get() = _showLoading
    val cart get() = _cart
    val likedProducts get() = _likedProducts


    fun getCartNoAuth() {
        viewModelScope.launch {
            val localCartProducts = App.INSTANCE.db.CartDao().getAll().map {
                RequestProductCart(it.productId, it.sizeId, it.colorId, it.count)
            }
            apiService.getCartNoAuth(localCartProducts)
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect { cartRemote ->
                    when (cartRemote) {
                        is Result.Success -> {
                            cartRemote.result.items = sortItems(cartRemote.result.items)
                                .filter { checkInCart(it) }
                            _cart.emit(cartRemote.result)
                        }

                        is Result.Error -> {}
                    }
                }
        }
    }

    private fun sortItems(items: List<CartItemRemote>): List<CartItemRemote> {
        return items.sortedWith(
            compareByDescending<CartItemRemote> { it.name }
                .thenByDescending { it.productId }
                .thenByDescending { it.productColor.id }
                .thenByDescending { it.productSize.id })
    }

    fun getLikedNoAuth() {
        viewModelScope.launch {
            val products = mutableListOf<ProductMainPreview>()
            val localLikedProducts =
                App.INSTANCE.db.FavoritesDao().getAllButCart().map { it.productId }
            val flows = localLikedProducts.map { apiService.getProductDetail(it) }
            flows.merge()
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            products.plusAssign(detailToPreview(it.result))
                        }

                        else -> {}
                    }
                }
            _likedProducts.emit(sortPreviewsItems(products)
                .filter { checkInFavorites(it.id) && !checkInCart(it.id) })
        }
    }
}