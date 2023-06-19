package ru.feip.elisianix.catalog.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.common.db.checkInCart
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.ActualBlocks
import ru.feip.elisianix.remote.models.Category
import ru.feip.elisianix.remote.models.CategoryMainPreview
import ru.feip.elisianix.remote.models.CategoryUtil
import ru.feip.elisianix.remote.models.MainBlock
import ru.feip.elisianix.remote.models.ProductsQueryMap
import ru.feip.elisianix.remote.models.dataClassToMap

class CatalogMainViewModel : ViewModel() {

    private val apiService = ApiService()
    private lateinit var catUtil: CategoryUtil

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _categories = MutableSharedFlow<List<Category>>(replay = 1)
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
                .collect { cats ->
                    when (cats) {
                        is Result.Success -> {
                            _categories.emit(cats.result.map { Category(it.id, it.name, it.image) })
                            catUtil = CategoryUtil(cats.result)
                            getProductActualBlock(new = true, discount = false)
                            getProductActualBlock(new = false, discount = true)
                            getProductCategoryBlocks(cats.result)
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
                            val productsTransform = it.result.products.map { prod ->
                                prod.copy(
                                    inCart = checkInCart(prod.id),
                                    inFavorites = checkInFavorites(prod.id)
                                )
                            }

                            if (new) {
                                actualBlocks.new = it.result.copy(products = productsTransform)
                            } else if (discount) {
                                actualBlocks.discount = it.result.copy(products = productsTransform)
                            }
                            _productActualBlocks.emit(actualBlocks)
                        }

                        is Result.Error -> {}
                    }
                }
        }
    }

    private fun getProductCategoryBlocks(categories: List<CategoryMainPreview>) {
        viewModelScope.launch {
            val blocks = mutableListOf<MainBlock>()

            val flows = categories.map {
                apiService.getProducts(
                    ProductsQueryMap(
                        categories = it.id.toString(), limit = 10, inSubCategories = true,
                    ).dataClassToMap()
                )
                    .onStart { _showLoading.value = true }
                    .onCompletion { _showLoading.value = false }
            }
            flows.merge()
                .collect {
                    when (it) {
                        is Result.Success -> {
                            val newProds = it.result.products.map { prod ->
                                prod.copy(
                                    inCart = checkInCart(prod.id),
                                    inFavorites = checkInFavorites(prod.id)
                                )
                            }
                            val cat = categories[catUtil.getLevel0Idx(newProds[0])]
                            blocks.plusAssign(
                                MainBlock(cat.id, cat.name, newProds, null)
                            )
                        }

                        else -> {}
                    }
                }
            val ids = categories.map { it.id }
            _productCategoryBlocks.emit(blocks.sortedBy { (ids + it.id).indexOf(it.id) })
        }
    }
}