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
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.RequestProductCart
import ru.feip.elisianix.remote.models.RequestProductCartUpdate

class CatalogSizeSelectorViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _productUpdatedInRemote = MutableSharedFlow<CartItem>(replay = 0)

    val showLoading get() = _showLoading
    val productUpdatedInRemote get() = _productUpdatedInRemote

    fun updateItemInRemoteCart(item: CartItem) {
        val add = RequestProductCart(item.productId, item.sizeId, item.colorId, 1)
        val remove = RequestProductCartUpdate(item.productId, item.sizeId, item.colorId, 0)
        viewModelScope.launch {
            when (checkInCartByInfo(item)) {
                true -> {
                    val newItem = item.copy(count = 0)
                    apiService.updateInRemoteCart(remove)
                        .onStart { _showLoading.value = true }
                        .onCompletion { _showLoading.value = false }
                        .collect {
                            when (it) {
                                is Result.Success -> _productUpdatedInRemote.emit(newItem)
                                is Result.Error -> {}
                            }
                        }
                }

                false -> {
                    val newItem = item.copy(count = 1)
                    apiService.addToRemoteCart(add)
                        .onStart { _showLoading.value = true }
                        .onCompletion { _showLoading.value = false }
                        .collect {
                            when (it) {
                                is Result.Success -> _productUpdatedInRemote.emit(newItem)
                                is Result.Error -> {}
                            }
                        }
                }
            }
        }
    }
}