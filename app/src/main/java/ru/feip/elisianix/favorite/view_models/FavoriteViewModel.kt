package ru.feip.elisianix.favorite.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

class FavoriteViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _favorites = MutableSharedFlow<List<ProductDetail>>(replay = 1)

    val showLoading get() = _showLoading
    val favorites get() = _favorites

    fun getFavoritesNoAuth(localFavoriteProducts: List<Int>) {
        val products = mutableListOf<ProductDetail>()
        viewModelScope.launch {
            coroutineScope {
                localFavoriteProducts.forEach { prodId ->
                    async {
                        apiService.getProductDetail(prodId)
                            .onStart { _showLoading.value = true }
                            .onCompletion { _showLoading.value = false }
                            .collect {
                                when (it) {
                                    is Result.Success -> {
                                        val prodTransform = it.result.copy(
                                            inCart = checkInCart(it.result.id),
                                            inFavorites = checkInFavorites(it.result.id)
                                        )
                                        products.plusAssign(prodTransform)
                                    }

                                    is Result.Error -> {}
                                }
                            }
                    }

                }
            }
            _favorites.emit(sortItems(products))
        }
    }

    private fun sortItems(items: List<ProductDetail>): List<ProductDetail> {
        return items.sortedWith(
            compareByDescending<ProductDetail> { it.name }.thenByDescending { it.id })
    }
}