package ru.feip.elisianix.catalog.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.common.db.checkInCart
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.ProductDetail
import ru.feip.elisianix.remote.models.ProductMainPreview

class CatalogProductViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _product = MutableSharedFlow<ProductDetail>(replay = 1)
    private val _productRecs = MutableSharedFlow<List<ProductMainPreview>>(replay = 1)

    val showLoading get() = _showLoading
    val product get() = _product
    val productRecs get() = _productRecs


    fun getProductDetail(productId: Int) {
        viewModelScope.launch {
            apiService.getProductDetail(productId)
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            _product.emit(it.result)
                        }

                        is Result.Error -> {}
                    }
                }
        }
    }

    fun getProductRecs(categoryId: Int) {
        viewModelScope.launch {
            apiService.getProductRecs(categoryId)
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            val productsTransform = it.result.products.map { prod ->
                                prod.copy(
                                    inCart = checkInCart(prod.id),
                                    inFavorites = checkInFavorites(prod.id)
                                )
                            }
                            _productRecs.emit(productsTransform)
                        }

                        is Result.Error -> {}
                    }
                }
        }
    }
}