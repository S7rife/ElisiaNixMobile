package ru.feip.elisianix.catalog.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.models.CategoryResponse

class CatalogMainViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _categories = MutableSharedFlow<List<CategoryResponse>>(replay = 0)

    val showLoading get() = _showLoading
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
}