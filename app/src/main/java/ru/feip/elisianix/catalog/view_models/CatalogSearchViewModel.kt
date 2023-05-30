package ru.feip.elisianix.catalog.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.CategoryMainPreview
import ru.feip.elisianix.remote.models.ProductMainPreview
import ru.feip.elisianix.remote.models.ProductsQueryMap
import ru.feip.elisianix.remote.models.dataClassToMap

class CatalogSearchViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _products = MutableSharedFlow<List<ProductMainPreview>>(replay = 0)
    private val _categories = MutableSharedFlow<List<CategoryMainPreview>>(replay = 0)

    val showLoading get() = _showLoading
    val products get() = _products
    val categories get() = _categories

    fun getCategories() {
        viewModelScope.launch {
            apiService.getCategories()
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            _categories.emit(it.result)
                        }
                        is Result.Error -> {}
                    }
                }
        }
    }

    fun getSearchProducts(searchQuery: String) {
        viewModelScope.launch {
            apiService.getProducts(ProductsQueryMap(filter = searchQuery).dataClassToMap())
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            println(it.result.products)
                            _products.emit(it.result.products)
                        }
                        is Result.Error -> {}
                    }
                }
        }
    }
}