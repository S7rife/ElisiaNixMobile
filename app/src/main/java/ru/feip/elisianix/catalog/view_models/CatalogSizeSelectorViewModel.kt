package ru.feip.elisianix.catalog.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.common.db.CartItem
import ru.feip.elisianix.common.db.checkInCartByInfo
import ru.feip.elisianix.common.db.editItemInCart
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.RequestProductCart
import ru.feip.elisianix.remote.models.RequestProductCartUpdate
import ru.feip.elisianix.remote.models.contains

class CatalogSizeSelectorViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _productUpdatedInRemote = MutableSharedFlow<Boolean>(replay = 0)

    val showLoading get() = _showLoading
    val productUpdatedInRemote get() = _productUpdatedInRemote

    fun updateItemInRemoteCart(item: CartItem) {
        val add = RequestProductCart(item.productId, item.sizeId, item.colorId, 1)
        val remove = RequestProductCartUpdate(item.productId, item.sizeId, item.colorId, 0)
        viewModelScope.launch {
            when (checkInCartByInfo(item)) {
                true -> {
                    apiService.updateInRemoteCart(remove)
                        .onStart { _showLoading.value = true }
                        .onCompletion { _showLoading.value = false }
                        .collect {
                            when (it) {
                                is Result.Success -> {
                                    editItemInCart(item, it.result.contains(item))
                                    _productUpdatedInRemote.emit(true)
                                }

                                is Result.Error -> {}
                            }
                        }
                }

                false -> {
                    apiService.addToRemoteCart(add)
                        .onStart { _showLoading.value = true }
                        .onCompletion { _showLoading.value = false }
                        .collect {
                            when (it) {
                                is Result.Success -> {
                                    editItemInCart(item, it.result.productsInBasketCount > 0)
                                    _productUpdatedInRemote.emit(true)
                                }

                                is Result.Error -> {}
                            }
                        }
                }
            }
        }
    }
}