package ru.feip.elisianix.favorite.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.common.App
import ru.feip.elisianix.common.db.checkInFavorites
import ru.feip.elisianix.common.db.detailToPreview
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.ProductMainPreview
import ru.feip.elisianix.remote.models.sortPreviewsItems

class FavoriteViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _favorites = MutableSharedFlow<List<ProductMainPreview>>(replay = 1)

    val showLoading get() = _showLoading
    val favorites get() = _favorites

    fun getFavoritesNoAuth() {
        viewModelScope.launch {
            val products = mutableListOf<ProductMainPreview>()
            val localFavoriteProducts = App.INSTANCE.db.FavoritesDao().getAll().map { it.productId }
            App.INSTANCE.db.FavoritesDao().getAllButCart().map { it.productId }
            val flows = localFavoriteProducts.map { apiService.getProductDetail(it) }
            flows.merge()
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            products.plusAssign(detailToPreview(it.result))
                        }

                        else -> {}
                    }
                }
            _favorites.emit(sortPreviewsItems(products).filter { checkInFavorites(it.id) })
        }
    }
}