package ru.feip.elisianix.start.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.common.db.CartItem
import ru.feip.elisianix.common.db.cartDao
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result

class SplashViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _cartUpdated = MutableSharedFlow<Boolean>(replay = 0)

    val showLoading get() = _showLoading
    val cartUpdated get() = _cartUpdated

    fun updateCartFromRemote() {
        viewModelScope.launch {
            apiService.getCartFromRemote()
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect { cart ->
                    when (cart) {
                        is Result.Success -> {
                            cartDao.deleteAll()
                            cart.result.items?.onEach {
                                cartDao.insert(
                                    CartItem(
                                        0,
                                        it.productId,
                                        it.productColor.id,
                                        it.productSize.id,
                                        1
                                    )
                                )
                            }
                            _cartUpdated.emit(true)
                        }

                        is Result.Error -> {
                            cartDao.deleteAll()
                            _cartUpdated.emit(true)
                        }
                    }
                }
        }
    }
}