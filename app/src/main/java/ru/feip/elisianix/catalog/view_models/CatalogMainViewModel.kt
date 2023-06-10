package ru.feip.elisianix.catalog.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.ActualBlocks
import ru.feip.elisianix.remote.models.CategoryMainPreview
import ru.feip.elisianix.remote.models.MainBlock
import ru.feip.elisianix.remote.models.ProductsQueryMap
import ru.feip.elisianix.remote.models.dataClassToMap

class CatalogMainViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _categories = MutableSharedFlow<List<CategoryMainPreview>>(replay = 1)
    private val _productActualBlocks = MutableSharedFlow<ActualBlocks>(replay = 1)
    private val _productCategoryBlocks = MutableSharedFlow<List<MainBlock>>(replay = 1)

    private val actualBlocks = ActualBlocks()

    val showLoading get() = _showLoading
    val categories get() = _categories
    val productActualBlocks get() = _productActualBlocks
    val productCategoryBlocks get() = _productCategoryBlocks

    fun getCategories() {
        viewModelScope.launch {
            apiService.getCategories()
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            _categories.emit(it.result)
                            getProductActualBlock(new = true, discount = false)
                            getProductActualBlock(new = false, discount = true)
                            getProductCategoryBlocks(it.result)
                        }

                        is Result.Error -> {}
                    }
                }
        }
    }

    private fun getProductActualBlock(new: Boolean, discount: Boolean) {
        viewModelScope.launch {
            apiService.getProducts(
                ProductsQueryMap(newProducts = new, discount = discount).dataClassToMap()
            )
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            if (new) {
                                actualBlocks.new = it.result
                            } else if (discount) {
                                actualBlocks.discount = it.result
                            }
                            _productActualBlocks.emit(actualBlocks)
                        }

                        is Result.Error -> {}
                    }
                }
        }
    }

    private fun getProductCategoryBlocks(categories: List<CategoryMainPreview>) {
        val blocks = mutableListOf<MainBlock>()
        viewModelScope.launch {
            coroutineScope {
                categories.forEach { cat ->
                    async {
                        apiService.getProducts(
                            ProductsQueryMap(categoryId = cat.id, limit = 10).dataClassToMap()
                        )
                            .onStart { _showLoading.value = true }
                            .onCompletion { _showLoading.value = false }
                            .collect {
                                when (it) {
                                    is Result.Success -> {
                                        blocks.add(
                                            MainBlock(
                                                cat.id,
                                                cat.name,
                                                it.result.products,
                                                null
                                            )
                                        )
                                    }

                                    is Result.Error -> {}
                                }
                            }
                    }
                }
            }
            val ids = categories.map { it.id }
            _productCategoryBlocks.emit(blocks.sortedBy { (ids + it.id).indexOf(it.id) })
        }
    }
}