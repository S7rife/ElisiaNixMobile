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
import ru.feip.elisianix.remote.models.*

class CatalogMainViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _categories = MutableSharedFlow<List<CategoryMainPreview>>(replay = 0)
    private val _newProducts = MutableSharedFlow<List<ProductMainPreview>>(replay = 0)
    private val _discountProducts = MutableSharedFlow<List<ProductMainPreview>>(replay = 0)
    private val _categoryBlockProducts = MutableSharedFlow<MainBlock>(replay = 0)

    val showLoading get() = _showLoading
    val categories get() = _categories
    val newProducts get() = _newProducts
    val discountProducts get() = _discountProducts
    val categoryBlockProducts get() = _categoryBlockProducts


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

    fun getNewProducts() {
        viewModelScope.launch {
            apiService.getProducts(ProductsQueryMap(newProducts = true).dataClassToMap())
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            _newProducts.emit(it.result.products)
                        }
                        is Result.Error -> {}
                    }
                }
        }
    }

    fun getDiscountProducts() {
        viewModelScope.launch {
            apiService.getProducts(ProductsQueryMap(discount = true).dataClassToMap())
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            _discountProducts.emit(it.result.products)
                        }
                        is Result.Error -> {}
                    }
                }
        }
    }

    fun getCategoryBlockProducts(categoryId: Int, categoryName: String) {
        viewModelScope.launch {
            apiService.getProducts(
                ProductsQueryMap(
                    categoryId = categoryId,
                    limit = 10
                ).dataClassToMap()
            )
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            _categoryBlockProducts.emit(
                                MainBlock(
                                    categoryId,
                                    categoryName,
                                    it.result.products
                                )
                            )
                        }
                        is Result.Error -> {}
                    }
                }
        }
    }
}