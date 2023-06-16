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
import ru.feip.elisianix.remote.models.Category
import ru.feip.elisianix.remote.models.ProductMainPreview
import ru.feip.elisianix.remote.models.ProductsQueryMap
import ru.feip.elisianix.remote.models.SearchSettings
import ru.feip.elisianix.remote.models.dataClassToMap

class CatalogCategoryViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _products = MutableSharedFlow<List<ProductMainPreview>>(replay = 1)
    private val _categories = MutableSharedFlow<List<Category>>(replay = 1)

    val showLoading get() = _showLoading
    val products get() = _products
    val categories get() = _categories

    fun getCategories() {
        viewModelScope.launch {
            apiService.getCategories()
                .collect { lst ->
                    when (lst) {
                        is Result.Success -> {
                            _categories.emit(lst.result.map { Category(it.id, it.name, it.image) })
                        }

                        is Result.Error -> {}
                    }
                }
        }
    }

    fun getProductsByFilters(ss: SearchSettings) {
        viewModelScope.launch {
            apiService.getProducts(
                ProductsQueryMap(
                    categories = ss.categoryId?.toString(),
                    brands = ss.brandId,
                    sortMethod = ss.sortMethod.value.second,
                    filter = ss.query
                ).dataClassToMap()
            )
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
                            _products.emit(productsTransform)
                        }

                        is Result.Error -> {}
                    }
                }
        }
    }
}