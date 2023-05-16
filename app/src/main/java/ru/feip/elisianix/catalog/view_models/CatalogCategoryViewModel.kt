package ru.feip.elisianix.catalog.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.ProductMainPreview
import ru.feip.elisianix.remote.models.ProductsQueryMap
import ru.feip.elisianix.remote.models.dataClassToMap

class CatalogCategoryViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _products = MutableSharedFlow<List<ProductMainPreview>>(replay = 0)

    val showLoading get() = _showLoading
    val products get() = _products

    fun getCategoryProducts(categoryId: Int) {
        viewModelScope.launch {
            apiService.getProducts(ProductsQueryMap(categoryId = categoryId).dataClassToMap())
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            _products.emit(it.result.products)
                        }
                        is Result.Error -> {}
                    }
                }
        }
    }
}