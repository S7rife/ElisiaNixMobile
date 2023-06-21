package ru.feip.elisianix.cart.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.feip.elisianix.remote.ApiService
import ru.feip.elisianix.remote.Result
import ru.feip.elisianix.remote.models.PickupPoint
import ru.feip.elisianix.remote.models.pickupPointParse

class MapViewModel : ViewModel() {
    private val apiService = ApiService()

    private val _showLoading = MutableStateFlow(false)
    private val _success = MutableStateFlow(false)
    private val _pickupPoints = MutableSharedFlow<List<PickupPoint>>(replay = 1)

    val showLoading get() = _showLoading
    val pickupPoints get() = _pickupPoints


    fun getPickupPoints() {
        viewModelScope.launch {
            apiService.getPickupPoints()
                .onStart { _showLoading.value = true }
                .onCompletion { _showLoading.value = false }
                .collect { res ->
                    when (res) {
                        is Result.Success -> {
                            _pickupPoints.emit(res.result.map { pickupPointParse(it) })
                        }

                        is Result.Error -> {}
                    }
                }
        }
    }
}