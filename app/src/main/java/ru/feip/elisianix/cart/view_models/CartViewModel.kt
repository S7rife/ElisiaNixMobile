package ru.feip.elisianix.cart.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.Cart
import ru.feip.elisianix.remote.models.CartItemRemote
import ru.feip.elisianix.remote.models.ProductMainPreview
import ru.feip.elisianix.remote.models.RequestProductCart

class CartViewModel : ViewModel() {
    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _cart = MutableSharedFlow<Cart>(replay = 1)
    private val _likedProducts = MutableSharedFlow<List<ProductMainPreview>>(replay = 0)

    val showLoading get() = _showLoading
    val cart get() = _cart
    val likedProducts get() = _likedProducts


    fun getCartNoAuth(localCartProducts: List<RequestProductCart>) {
        viewModelScope.launch {
            apiService.getCartNoAuth(localCartProducts)
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            it.result.items = sortItems(it.result.items)
                            _cart.emit(it.result)
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
}