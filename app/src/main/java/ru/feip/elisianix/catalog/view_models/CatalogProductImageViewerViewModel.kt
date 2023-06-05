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
import ru.feip.elisianix.remote.models.Image

class CatalogProductImageViewerViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _images = MutableSharedFlow<List<Image>>(replay = 0)

    val showLoading get() = _showLoading
    val images get() = _images


    fun getProductImages(productId: Int) {
        viewModelScope.launch {
            apiService.getProductDetail(productId)
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect {
                    when (it) {
                        is Result.Success -> {
                            _images.emit(it.result.images)
                        }
                        is Result.Error -> {}
                    }
                }
        }
    }
}